package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import be.cytomine.processing.Software
import be.cytomine.project.Project

import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.security.SecUserSecRole
import groovyx.gpars.Asynchronizer
import java.util.concurrent.Future

class RestJobController extends RestController {

    def jobService
    def softwareService
    def projectService
    def jobParameterService
    def retrievalSuggestTermJobService
    def userService
    def backgroundService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        log.info "list all job"
        if(params.software!=null) {
            Software software = Software.read(params.software)
            if(software) responseSuccess(jobService.list(software,false,Integer.MAX_VALUE))
            else responseNotFound("Job", "Software", params.software)
        } else responseSuccess(jobService.list())
    }

    def listByProject = {
        log.info "list all job by project"
        boolean light = params.light==null? false : params.boolean('light')
        int max = params.max==null? Integer.MAX_VALUE : params.int('max')

        Project project = projectService.read(params.long('id'), new Project())
        if(project) {
            log.info "project="+project.id + " software="+params.software
            if(params.software!=null) {
                Software software = Software.read(params.software)
                if(software) responseSuccess(jobService.list(software,project,light,max))
                else responseNotFound("Job", "Software", params.software)
            }
            else responseSuccess(jobService.list(project,light,max))
        } else responseNotFound("Job", "Project", params.id)
    }

    def listBySoftware = {
        Software software = softwareService.read(params.long('id'));
        if (software) responseSuccess(jobService.list(software,false,Integer.MAX_VALUE))
        else responseNotFound("Job", "Software", params.id)
    }

    def listBySoftwareAndProject = {
        Software software = softwareService.read(params.long('idSoftware'));
        Project project = projectService.read(params.long('idProject'), new Project());
        if (!software) responseNotFound("Job", "Software", params.idSoftware)
        if (!project) responseNotFound("Job", "Project", params.idProject)
        else responseSuccess(jobService.list(software, project,false,Integer.MAX_VALUE))
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
        Project project = projectService.read(params.long('id'),new Project());
        if (!project) responseNotFound("Job", "Project", params.id)
        else {
            Long idSoftware = Long.parseLong(params.software)
            //TODO: execute retrieval should be a generic method execute
            /*if(idSoftware==Software.findByServiceNameIlike("retrievalSuggestedTermJobService").id) {
                Software software = Software.findByName("Retrieval-Suggest-Term")
                def resp = createAndExecuteJob(project, software)
                responseSuccess(resp)
            }
            if(idSoftware==Software.findByServiceNameIlike("pyxitSuggestedTermJobService").id) {
                Software software = Software.findByName("Pyxit KFOLDS")
                def resp = createAndExecuteJob(project, software)
                responseSuccess(resp)
            }*/
            Software software = Software.read(idSoftware)
            if (!software) responseNotFound("Job", "Software", params.id)
            def resp = createAndExecuteJob(project, software)
            responseSuccess(resp)
        }
    }

    private def createAndExecuteJob(Project project, Software software) {
        //create Job
        log.info "Create Job..."
        Job job = new Job(project: project, software: software,running:true)
        def result = jobService.add(JSON.parse(job.encodeAsJSON()))
        log.info "result=" + result
        log.info "result=" + Long.parseLong(result.data.job.id.toString())
        job = Job.get(Long.parseLong(result.data.job.id.toString()))
        Job.list().each {
            println "JOB=" + it.id + " " + it.class
        }
        log.info "result=" + job

        //Create User-job
        UserJob userJob = createUserJob(User.read(springSecurityService.principal.id), job)
        software.service.init(job, userJob)

        log.info "### Launch asynchronous..."
        backgroundService.execute("Retrieval-suggest asynchronously", {
            log.info "Launch thread";
            software.service.execute(job)
        })
        log.info "### Return response..."
        job
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

        return userJob
    }
}
