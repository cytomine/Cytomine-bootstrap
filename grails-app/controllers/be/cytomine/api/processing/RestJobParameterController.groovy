package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for job parameter
 * Each software may have some parameters (e.g.: cytomine project id, number of thread,...)
 * A job parameter is a software parameter instance with a specific value for this job
 */
@Api(name = "job parameter services", description = "Methods for managing job parameter. Each software may have some parameters (e.g.: cytomine project id, number of thread,...). A job parameter is a software parameter instance with a specific value for this job")
class RestJobParameterController extends RestController {

    def jobParameterService
    def jobService

    /**
     * List all job parameter
     */
    @ApiMethodLight(description="Get all job parameter", listing = true)
    def list() {
        responseSuccess(jobParameterService.list())
    }

    /**
     * List all job parameter for a job
     */
    @ApiMethodLight(description="Get all job parameter for a job", listing = true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job id")
    ])
    def listByJob() {
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
    @ApiMethodLight(description="Get a job parameter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job parameter id")
    ])
    def show() {
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
    @ApiMethodLight(description="Add a new job parameter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job parameter id")
    ])
    def add() {
        add(jobParameterService, request.JSON)
    }

    /**
     * Update job parameter
     */
    @ApiMethodLight(description="Update a job parameter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job parameter id")
    ])
    def update() {
        update(jobParameterService, request.JSON)
    }

    /**
     * Delete job parameter
     */
    @ApiMethodLight(description="Delete a job parameter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job parameter id")
    ])
    def delete() {
        delete(jobParameterService, JSON.parse("{id : $params.id}"),null)
    }

}
