package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import grails.converters.JSON

/**
 * Controller for software parameter
 * A software may have some parameter (thread number, project id,...).
 * When a software is running, a job is created. Each software parameter will produced a job parameter with a specific value.
 */
class RestSoftwareParameterController extends RestController{

    def softwareParameterService

    /**
     * List all software parameter
     */
    def list = {
        responseSuccess(softwareParameterService.list())
    }

    /**
     * List all sofwtare parameter for a single software
     */
    def listBySoftware = {
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
    def show = {
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
    def add = {
        add(softwareParameterService, request.JSON)
    }

    /**
     * Update a software parameter
     */
    def update = {
        update(softwareParameterService, request.JSON)
    }

    /**
     * Delete a software parameter
     */
    def delete = {
        delete(softwareParameterService, JSON.parse("{id : $params.id}"),null)
    }
}
