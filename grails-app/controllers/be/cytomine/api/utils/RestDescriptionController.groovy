package be.cytomine.api.utils

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for a description (big text data/with html format) on a specific domain
 */
@RestApi(name = "description services", description = "Methods for managing description on a specific domain")
class RestDescriptionController extends RestController {

    def springSecurityService
    def descriptionService

    @RestApiMethod(description="List all description available", listing=true)
    def list() {
        responseSuccess(descriptionService.list())
    }

    @RestApiMethod(description="Get a description for a specific domain (id and class)")
    @RestApiParams(params=[
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class")
    ])
    def showByDomain() {
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
    @RestApiMethod(description="Add a new description to a domain")
    def add() {
        add(descriptionService, request.JSON)
    }

    /**
     * Update a description
     */
    @RestApiMethod(description="Update a description")
    @RestApiParams(params=[
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class")
    ])
    def update() {
        update(descriptionService, request.JSON)
    }

    /**
     * Delete a description
     */
    @RestApiMethod(description="Delete a description")
    @RestApiParams(params=[
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class")
    ])
    def delete() {
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

