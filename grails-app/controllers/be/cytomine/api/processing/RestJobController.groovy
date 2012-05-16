package be.cytomine.api.processing

import grails.plugins.springsecurity.Secured

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import grails.converters.JSON

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
        boolean light = params.light==null ? false : params.boolean('light')
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
        try {
            log.debug("add")
            def result = jobService.add(request.JSON)
            log.debug("result="+result)
            def idJob = result?.data?.job?.id
            log.debug("idJob="+idJob)
            executeJob(Job.get(idJob))
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    private def executeJob(Job job) {
        log.info "Create UserJob..."
        UserJob userJob = createUserJob(User.read(springSecurityService.principal.id), job)
        job.software.service.init(job, userJob)

        log.info "Launch async..."
        backgroundService.execute("RunJobAsynchronously", {
            log.info "Launch thread";
            job.software.service.execute(job)
        })
        job
    }

    def update = {
        log.info "update job controller"
        update(jobService, request.JSON)
    }

    def delete = {
        delete(jobService, JSON.parse("{id : $params.id}"))
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
