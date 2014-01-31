package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for software parameter
 * A software may have some parameter (thread number, project id,...).
 * When a software is running, a job is created. Each software parameter will produced a job parameter with a specific value.
 */
@Api(name = "software parameter services", description = "Methods for software parameters, a software may have some parameter (thread number, project id,...). When a software is running, a job is created. Each software parameter will produced a job parameter with a specific value.")
class RestSoftwareParameterController extends RestController{

    def softwareParameterService

    /**
     * List all software parameter
     */
    @ApiMethodLight(description="Get all software parameter", listing = true)
    def list() {
        responseSuccess(softwareParameterService.list())
    }

    /**
     * List all sofwtare parameter for a single software
     */
    @ApiMethodLight(description="Get all software parameters for a software", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software id"),
        @ApiParam(name="setByServer", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) Include params set by server"),
    ])
    def listBySoftware() {
        Software software = Software.read(params.long('id'))
        boolean includeSetByServer = params.boolean('setByServer', false)
        if(software) {
            responseSuccess(softwareParameterService.list(software, includeSetByServer))
        } else {
            responseNotFound("Software", params.id)
        }
    }

    /**
     * Get a software parameter info
     */
    @ApiMethodLight(description="Get a software parameter info")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software id")
    ])
    def show() {
        SoftwareParameter parameter = softwareParameterService.read(params.long('id'))
        if (parameter) {
            responseSuccess(parameter)
        } else {
            responseNotFound("SoftwareParameter", params.id)
        }
    }

    /**
     * Add a new software parameter
     */
    @ApiMethodLight(description="Add a new software parameter")
    def add() {
        add(softwareParameterService, request.JSON)
    }

    /**
     * Update a software parameter
     */
    @ApiMethodLight(description="Update a software parameter")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software parameter id")
    ])
    def update() {
        update(softwareParameterService, request.JSON)
    }

    /**
     * Delete a software parameter
     */
    @ApiMethodLight(description="Delete a software parameter")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software parameter id")
    ])
    def delete() {
        delete(softwareParameterService, JSON.parse("{id : $params.id}"),null)
    }
}
