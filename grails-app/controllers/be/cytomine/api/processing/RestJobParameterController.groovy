package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import grails.converters.JSON

/**
 * Controller for job parameter
 * Each software may have some parameters (e.g.: cytomine project id, number of thread,...)
 * A job parameter is a software parameter instance with a specific value for this job
 */
class RestJobParameterController extends RestController {

    def jobParameterService
    def jobService

    /**
     * List all job parameter
     */
    def list = {
        responseSuccess(jobParameterService.list())
    }

    /**
     * List all job parameter for a job
     */
    def listByJob = {
        Job job = jobService.read(params.long('id'));
        if (job) {
            responseSuccess(jobParameterService.list(job))
        } else {
            responseNotFound("JobParameter", "Job", params.id)
        }
    }

    /**
     * Get a job parameter
     */
    def show = {
        JobParameter jobParameter = jobParameterService.read(params.long('id'))
        if (jobParameter) {
            responseSuccess(jobParameter)
        } else {
            responseNotFound("JobParameter", params.id)
        }
    }

    /**
     * Add a new job parameter
     */
    def add = {
        add(jobParameterService, request.JSON)
    }

    /**
     * Update job parameter
     */
    def update = {
        update(jobParameterService, request.JSON)
    }

    /**
     * Delete job parameter
     */
    def delete = {
        delete(jobParameterService, JSON.parse("{id : $params.id}"),null)
    }

}
