package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import be.cytomine.processing.JobParameter

class RestJobParameterController extends RestController {

    def jobParameterService
    def jobService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(jobParameterService.list())
    }

    def listByJob = {
        Job job = jobService.read(params.long('id'));
        if (job) responseSuccess(jobParameterService.list(job))
        else responseNotFound("JobParameter", "Job", params.id)
    }
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        JobParameter jobParameter = jobParameterService.read(params.long('id'))
        if (jobParameter) responseSuccess(jobParameter)
        else responseNotFound("JobParameter", params.id)
    }

    def add = {
        add(jobParameterService, request.JSON)
    }

    def update = {
        update(jobParameterService, request.JSON)
    }

    def delete = {
        delete(jobParameterService, JSON.parse("{id : $params.id}"))
    }

}
