package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

/**
 * Controller for software: application that can be launch (job)
 */
class RestSoftwareController extends RestController {

    def softwareService
    def jobService

    /**
     * List all software available in cytomine
     */
    def list = {
        responseSuccess(softwareService.list())
    }

    /**
     * List all software by project
     */
    def listByProject = {
        Project project = Project.read(params.long('id'))
        if(project) responseSuccess(softwareService.list(project))
        else responseNotFound("Project", params.id)
    }

    /**
     * Get a specific software
     */
    def show = {
        Software software = softwareService.read(params.long('id'))
        if (software) {
            responseSuccess(software)
        } else {
            responseNotFound("Software", params.id)
        }
    }

    /**
     * Add a new software to cytomine
     * We must add in other request: parameters, software-project link,...
     */
    def add = {
        add(softwareService, request.JSON)
    }

    /**
     * Update a software info
     */
    def update = {
        update(softwareService, request.JSON)
    }

    /**
     * Delete software
     */
    def delete = {
        delete(softwareService, JSON.parse("{id : $params.id}"))
    }

    /**
     * List software
     * TODO:: could be improved with a single SQL request
     *
     */
    def softwareInfoForProject = {
        Project project = Project.read(params.long('idProject'))
        Software software = Software.read(params.long('idSoftware'))
        if(!project) {
            responseNotFound("Project", params.idProject)
        } else if(!software) {
            responseNotFound("Software", params.idSoftware)
        } else {
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
