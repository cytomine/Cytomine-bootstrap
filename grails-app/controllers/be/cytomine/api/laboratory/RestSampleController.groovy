package be.cytomine.api.laboratory

import be.cytomine.api.RestController
import be.cytomine.laboratory.Sample
import grails.converters.JSON

/**
 * Controller for sample (part of 'source' that has been scan to image)
 */
class RestSampleController extends RestController {

    def springSecurityService
    def secUserService
    def sampleService
    def cytomineService

    /**
     * List all available sample for the current user
     */
    def list = {
        responseSuccess(sampleService.list(cytomineService.getCurrentUser()))
    }

    /**
     * Get a sample
     */
    def show = {
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
    def add = {
        add(sampleService, request.JSON)
    }

    /**
     * Update a existing sample
     */
    def update = {
        update(sampleService, request.JSON)
    }

    /**
     * Delete sample
     */
    def delete = {
        delete(sampleService, JSON.parse("{id : $params.id}"),null)
    }
}
