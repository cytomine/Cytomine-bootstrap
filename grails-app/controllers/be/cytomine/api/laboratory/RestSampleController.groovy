package be.cytomine.api.laboratory

import be.cytomine.api.RestController
import be.cytomine.laboratory.Sample
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for sample (part of 'source' that has been scan to image)
 */
@Api(name = "sample services", description = "Methods for managing a sample, part of 'source' that has been scan to image")
class RestSampleController extends RestController {

    def sampleService
    def cytomineService

    /**
     * List all available sample for the current user
     */
    @ApiMethodLight(description="Get all sample available for the current user", listing = true)
    def list() {
        responseSuccess(sampleService.list(cytomineService.getCurrentUser()))
    }

    /**
     * Get a sample
     */
    @ApiMethodLight(description="Get a sample")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The sample id")
    ])
    def show() {
        Sample sample = sampleService.read(params.long('id'))
        if (sample) {
            responseSuccess(sample)
        } else {
            responseNotFound("Sample", params.id)
        }
    }

    /**
     * Add a new sample
     */
    @ApiMethodLight(description="Add a new sample")
    def add() {
        add(sampleService, request.JSON)
    }

    /**
     * Update a existing sample
     */
    @ApiMethodLight(description="Update a sample")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The sample id")
    ])
    def update() {
        update(sampleService, request.JSON)
    }

    /**
     * Delete sample
     */
    @ApiMethodLight(description="Delete a sample")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The sample id")
    ])
    def delete() {
        delete(sampleService, JSON.parse("{id : $params.id}"),null)
    }
}
