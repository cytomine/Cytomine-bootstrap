package be.cytomine.api.utils

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController

/**
 * Controller for a description (big text data/with html format) on a specific domain
 */
class RestDescriptionController extends RestController {

    def springSecurityService
    def descriptionService

    def list = {
        responseSuccess(descriptionService.list())
    }

    def showByDomain = {
        println "showByDomain"
        def id = params.long('domainIdent')
        def className = params.get('domainClassName')

        if(className && id) {
            def desc = descriptionService.get(id,className.replace("_","."))
            if(desc) {
                responseSuccess(desc)
            } else {
                responseNotFound("Domain","$id and className=$className")
            }
        } else {
            responseNotFound("Domain","$id and className=$className")
        }

    }

    /**
     * Add a new description to a domain
     */
    def add = {
        add(descriptionService, request.JSON)
    }

    /**
     * Update a description
     */
    def update = {
        update(descriptionService, request.JSON)
    }

    /**
     * Delete a description
     */
    def delete = {
        try {
            def json = [domainIdent:params.long('domainIdent'),domainClassName:params.get('domainClassName').replace("_",".")]
            def domain = descriptionService.retrieve(json)
            def result = descriptionService.delete(domain,transactionService.start(),null,true)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }
}

