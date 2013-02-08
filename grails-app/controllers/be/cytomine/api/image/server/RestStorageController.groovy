package be.cytomine.api.image.server

import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityCheck
import be.cytomine.api.RestController

import be.cytomine.image.server.Storage
import be.cytomine.utils.Task
import grails.converters.JSON

class RestStorageController extends RestController {

    def cytomineService
    def storageService

    /**
     * List all project available for the current user
     */
    def list = {
        log.info 'listing storages'
        responseSuccess(storageService.list())
    }

    /**
     * Get a project
     */
    def show = {
        Storage storage = storageService.read(params.long('id'))
        if (storage) {
            responseSuccess(storage)
        } else {
            responseNotFound("Storage", params.id)
        }
    }

    /**
     * Add a new storage to cytomine
     */
    def add = {
        add(storageService, request.JSON)
    }

    /**
     * Update a storage
     */
    def update = {
        try {
            def domain = storageService.retrieve(request.JSON)
            def result = storageService.update(request.JSON,new SecurityCheck(domain))
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    /**
     * Delete a storage
     */
    def delete = {
        try {
            def domain = storageService.retrieve(JSON.parse("{id : $params.id}"))
            def result = storageService.delete(JSON.parse("{id : $params.id}"),new SecurityCheck(domain), null)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }
}
