package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.processing.JobParameter
import be.cytomine.processing.SoftwareParameter
import be.cytomine.processing.job.RetrievalSuggestTermJob
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.security.SecUserSecRole

class RestJobController extends RestController {

    def jobService
    def softwareService
    def projectService
    def jobParameterService
    def retrievalSuggestTermJobService
    def userService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        log.info "list all job"
        if(params.software!=null) {
            if(params.software.equals("retrieval")) responseSuccess(retrievalSuggestTermJobService.list())
        } else responseSuccess(jobService.list())
    }

    def listByProject = {
        log.info "list all job by project"
        Project project = projectService.read(params.long('id'))
        if(project) {
            log.info "project="+project.id + " software="+params.software
            if(params.software!=null) {
                if(params.software.equals("retrieval")) responseSuccess(retrievalSuggestTermJobService.list(project))
            } else responseSuccess(jobService.list(project))
        } else responseNotFound("Job", "Project", params.id)
    }

    def listBySoftware = {
        Software software = softwareService.read(params.long('id'));
        if (software) responseSuccess(jobService.list(software))
        else responseNotFound("Job", "Software", params.id)
    }

    def listBySoftwareAndProject = {
        Software software = softwareService.read(params.long('idSoftware'));
        Project project = projectService.read(params.long('idProject'));
        if (!software) responseNotFound("Job", "Software", params.idSoftware)
        if (!project) responseNotFound("Job", "Project", params.idProject)
        else responseSuccess(jobService.list(software, project))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Job job = jobService.read(params.long('id'))
        if (job) responseSuccess(job)
        else responseNotFound("Job", params.id)
    }

    def add = {
        add(jobService, request.JSON)
    }

    def update = {
        log.info "update job controller"
        update(jobService, request.JSON)
    }

    def delete = {
        delete(jobService, JSON.parse("{id : $params.id}"))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def listRetrieval = {
        log.info "list all job"
        responseSuccess(retrievalSuggestTermJobService.list())
    }

    def execute = {
        Project project = projectService.read(params.long('id'));
        if (!project) responseNotFound("Job", "Project", params.id)
        else {
            if(params.software.equals("retrieval")) {
                def resp = executeRetrieval(project)
                responseSuccess(resp)
            }
            else responseNotFound("Job", "Software", params.id)
        }
    }

    def executeRetrieval(Project project) {

            //Retrieve software retrieval-suggest-algo
            log.info "Retrieve software: Retrieval-Suggest-Term..."
            Software software = Software.findByName("Retrieval-Suggest-Term")

            //create Job
            log.info "Create Job..."
            RetrievalSuggestTermJob job = new RetrievalSuggestTermJob(project: project, software: software)
            def result = retrievalSuggestTermJobService.add(JSON.parse(job.encodeAsJSON()))
            log.info "result=" + result
            log.info "result=" + Long.parseLong(result.data.retrievalsuggesttermjob.id.toString())
            job = RetrievalSuggestTermJob.get(Long.parseLong(result.data.retrievalsuggesttermjob.id.toString()))
            Job.list().each {
                println "JOB=" + it.id + " " + it.class
            }
            log.info "result=" + job

            //Create User-job
            UserJob userJob = createUserJob(User.read(springSecurityService.principal.id), job)
            /*
                * 0: type (=> cytomine) or standalone if execute with ide/java -jar  => STRING
                * 1: public key
                * 2: private key
                * 3: N value
                * 4: T value
                * 5: Working dir
                * 6: Cytomine Host
                * 7: Force download crop (even if already exist) => BOOLEAN
                * 8: storeName (KYOTOSINGLEFILE)
                * 9: index project (list: x,y,z)
                * 10: search project (only one)
             */
            //Create Job-parameter
            jobParameterService.add(JSON.parse(createParam("execType",job,"cytomine").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("publicKey",job,userJob.publicKey).encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("privateKey",job,userJob.privateKey).encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("N",job, "500").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("T",job, "5").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("workingDir",job, "/home/lrollus/Cytomine/algo/suggest/").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("cytomineHost",job, "http://localhost:8080").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("forceDownloadCrop",job, "false").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("storeName",job, "KYOTOSINGLEFILE").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("indexProject",job, "57").encodeAsJSON()))
            jobParameterService.add(JSON.parse(createParam("searchProject",job, "57").encodeAsJSON()))
            //Execute Job
            log.info "Execute Job..."
            job.execute()
            responseSuccess(job)
    }

    public JobParameter createParam(String name, Job job, String value) {
        SoftwareParameter softwareParameter = SoftwareParameter.findBySoftwareAndName(job.software, name)
        JobParameter jobParam = new JobParameter(value: value, job: job, softwareParameter: softwareParameter)
        return  jobParam
    }


    public UserJob createUserJob(User user, Job job) {
        UserJob userJob = new UserJob()
        userJob.job = job
        userJob.username = "JOB[" + user.username + " ], " + new Date().toString()
        userJob.password = user.password
        userJob.generateKeys()
        userJob.enabled = user.enabled
        userJob.accountExpired = user.accountExpired
        userJob.accountLocked = user.accountLocked
        userJob.passwordExpired = user.passwordExpired
        userJob.user = user
        userJob = userJob.save(flush: true)

        user.getAuthorities().each { secRole ->
            SecUserSecRole.create(userJob, secRole)
        }

        projectService.list().each {
            userService.addUserFromProject(userJob, it, true)
        }

        return userJob
    }
}
