package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Software
import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.processing.Job

class RestSoftwareController extends RestController {

    def softwareService
    def jobService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(softwareService.list())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def listByProject = {
        Project project = Project.read(params.long('id'))
        if(project) responseSuccess(softwareService.list(project))
        else responseNotFound("Project", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Software software = softwareService.read(params.long('id'))
        if (software) responseSuccess(software)
        else responseNotFound("Software", params.id)
    }

    def add = {
        add(softwareService, request.JSON)
    }

    def update = {
        log.info "update software controller"
        update(softwareService, request.JSON)
    }

    def delete = {
        delete(softwareService, JSON.parse("{id : $params.id}"))
    }

    def softwareInfoForProject = {
        Project project = Project.read(params.long('idProject'))
        Software software = Software.read(params.long('idSoftware'))
        if(!project) responseNotFound("Project", params.idProject)
        else if(!software) responseNotFound("Software", params.idSoftware)
        else {
            def result = [:]
            List<Job> jobs = Job.findAllByProjectAndSoftware(project,software)
            
            //Number of job for this software and this project
            result['numberOfJob'] = jobs.size()
            
            //Number of job by state
            result['numberOfNotLaunch'] = 0
            result['numberOfInQueue'] = 0
            result['numberOfRunning'] = 0
            result['numberOfSuccess'] = 0
            result['numberOfFailed'] = 0
            result['numberOfIndeterminate'] = 0
            result['numberOfWait'] = 0
            
            jobs.each { job ->
                if(job.status==Job.NOTLAUNCH) result['numberOfNotLaunch']++
                if(job.status==Job.INQUEUE) result['numberOfInQueue']++
                if(job.status==Job.RUNNING) result['numberOfRunning']++
                if(job.status==Job.SUCCESS) result['numberOfSuccess']++
                if(job.status==Job.FAILED) result['numberOfFailed']++
                if(job.status==Job.INDETERMINATE) result['numberOfIndeterminate']++
                if(job.status==Job.WAIT) result['numberOfWait']++
            }

            responseSuccess(result)
        }





    }
}
