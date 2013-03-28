package be.cytomine.api.security

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import groovy.sql.Sql
import javassist.tools.rmi.ObjectNotFoundException

import java.text.SimpleDateFormat

/**
 * Handle HTTP Requests for CRUD operations on the User Job domain class.
 */
class RestUserJobController extends RestController {

    def springSecurityService
    def cytomineService
    def secUserService
    def projectService
    def imageInstanceService
    def jobService
    def dataSource

    /**
     * Get a user job
     */
    def showUserJob = {
        UserJob userJob = UserJob.read(params.long('id'))
        if (userJob) {
            responseSuccess(userJob)
        } else {
            responseNotFound("UserJob", params.id)
        }
    }


    /**
     * Create a new user job for algo
     */
    def createUserJob = {
        def json = request.JSON
            try {
                //get user job parent
                User user
                if (json.parent.toString().equals("null")) {
                    user = User.read(springSecurityService.principal.id)
                } else {
                    user = User.read(json.parent.toString())
                }

                //get job for this user
                Job job
                if (json.job.toString().equals("null")) {
                    //Job is not defined, create a new one
                    log.debug "create new job:" + json
                    job = createJob(json.software, json.project)
                } else {
                    log.debug "add job " + json.job + " to userjob"
                    //Job is define, juste get it
                    job = Job.get(Long.parseLong(json.job.toString()))
                }

                //create user job
                UserJob userJob = addUserJob(user, job, json)

                response([userJob: userJob], 200)
            } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
         }
    }

    /**
     * List user job for a project (in list or tree format)
     * With image filter, list only user job that has added annotation on this image (with list)
     * TODO:: should be optim!!!
     * -filter project + tree => Job.findAllByProjectAndSoftware should be replace by sql request
     * -filter image + list =>  Job.findAllByProject & countByUserAndImage should be replace by SQL request
     * -no filter =>  findAllByProject => idem sql request
     */
    def listUserJobByProject = {
        Project project = projectService.read(params.long('id'))

        if (project) {
            if (params.getBoolean("tree")) {
                //list like tree with software as parent and job as leaf
                SimpleDateFormat formater = new SimpleDateFormat("dd MM yyyy HH:mm:ss")

                def root = [:]
                root.isFolder = true
                root.hideCheckbox = true
                root.name = project.name
                root.title = project.name
                root.key = project.id
                root.id = project.id

                def allSofts = []
                List<SoftwareProject> softwareProject = SoftwareProject.findAllByProject(project)

                softwareProject.each {
                    Software software = it.software
                    def soft = [:]
                    soft.isFolder = true
                    soft.name = software.name
                    soft.title = software.name
                    soft.key = software.id
                    soft.id = software.id
                    soft.hideCheckbox = true

                    def softJob = []
                    //TODO:: must be optim!!! (see method head comment)
                    List<Job> jobs = Job.findAllByProjectAndSoftware(project, software, [sort: 'created', order: 'desc'])
                    jobs.each {
                        def userJob = UserJob.findByJob(it);
                        def job = [:]
                        if (userJob) {
                            job.id = userJob.id
                            job.key = userJob.id
                            job.title = formater.format(it.created);
                            job.date = it.created.getTime()
                            job.isFolder = false
                            //job.children = []
                            softJob << job
                        }
                    }
                    soft.children = softJob

                    allSofts << soft

                }
                root.children = allSofts
                responseSuccess(root)

            } else if (params.getLong("image")) {
                //just get user job that add data to images

                def image = imageInstanceService.read(params.getLong("image"))
                if (!image) {
                    throw new ObjectNotFoundException("Image ${params.image} was not found!")
                }
                //TODO:: should be optim!!! (see method head comment)


                //better perf with sql request
                String request = "SELECT sec_user.id as idUser, job.id as idJob, software.id as idSoftware, software.name as softwareName, extract(epoch from job.created)*1000 as created,job.data_deleted as deleted "+
                                 "FROM job, sec_user, software " +
                                 "WHERE job.project_id = ${project.id} " +
                                 "AND job.id = sec_user.job_id " +
                                 "AND job.software_id = software.id "+
                                 "AND sec_user.id IN (SELECT DISTINCT user_id FROM algo_annotation WHERE image_id = ${image.id}) "+
                                 "ORDER BY job.created DESC"
                def data = []
                new Sql(dataSource).eachRow(request) {
                    def item = [:]
                    item.id = it.idUser
                    item.idJob = it.idJob
                    item.idSoftware = it.idSoftware
                    item.softwareName = it.softwareName
                    item.created = it.created
                    item.algo = true
                    item.isDeleted = it.deleted
                    data << item
                }
                responseSuccess(data)
            } else {
                def userJobs = []
                //TODO:: should be optim (see method head comment)
                List<Job> allJobs = Job.findAllByProject(project, [sort: 'created', order: 'desc'])

                allJobs.each { job ->
                    def item = [:]
                    def userJob = UserJob.findByJob(job);
                    if (userJob) {
                        item.id = userJob.id
                        item.idJob = job.id
                        item.idSoftware = job.software.id
                        item.softwareName = job.software.name
                        item.created = job.created.getTime()
                        item.algo = true
                        item.isDeleted = job.dataDeleted
                    }
                    userJobs << item
                }
                responseSuccess(userJobs)
            }
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Create a new job for this software and this project
     * @param idSoftware job software
     * @param idProject job project
     * @return Job created
     */
    private Job createJob(def idSoftware, def idProject) {
        Job job = new Job()
        job.software = Software.read(idSoftware)
        job.project = Project.read(idProject)
        jobService.saveDomain(job)
        job
    }

    /**
     * Create a new user job for this user, this job
     * @param user User that create this user job
     * @param job Job to link with user job
     * @param json JSON extra info
     * @return User job created
     */
    private UserJob addUserJob(def user, def job, def json) {
        //create user job
        log.debug "Create userJob"
        UserJob userJob = new UserJob()
        userJob.username = "JOB[" + user.username + "], " + new Date().toString()
        userJob.password = user.password
        userJob.generateKeys()
        userJob.enabled = user.enabled
        userJob.accountExpired = user.accountExpired
        userJob.accountLocked = user.accountLocked
        userJob.passwordExpired = user.passwordExpired
        userJob.user = user
        userJob.job = job
        Date date = new Date()

        try {
            date.setTime(Long.parseLong(json.created.toString()))
        } catch(Exception e) {

        }
        userJob.created = date
        jobService.saveDomain(userJob)

        //add the same role to user job
        user.getAuthorities().each { secRole ->
            SecUserSecRole.create(userJob, secRole)
        }
        return userJob
    }
}
