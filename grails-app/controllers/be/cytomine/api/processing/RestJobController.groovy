package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import be.cytomine.processing.Software
import be.cytomine.project.Project

class RestJobController extends RestController {

    def jobService
    def softwareService
    def projectService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        log.info "list all job"
        responseSuccess(jobService.list())
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
        else responseSuccess(jobService.list(software,project))
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

}
