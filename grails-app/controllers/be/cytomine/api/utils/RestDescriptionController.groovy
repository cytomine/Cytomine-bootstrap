package be.cytomine.api.utils

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for a description (big text data/with html format) on a specific domain
 */
@Api(name = "description services", description = "Methods for managing description on a specific domain")
class RestDescriptionController extends RestController {

    def springSecurityService
    def descriptionService

    @ApiMethodLight(description="List all description available", listing=true)
    def list() {
        responseSuccess(descriptionService.list())
    }

    @ApiMethodLight(description="Get a description for a specific domain (id and class)")
    @ApiParams(params=[
        @ApiParam(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParam(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class")
    ])
    def showByDomain() {
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
    @ApiMethodLight(description="Add a new description to a domain")
    def add() {
        add(descriptionService, request.JSON)
    }

    /**
     * Update a description
     */
    @ApiMethodLight(description="Update a description")
    @ApiParams(params=[
        @ApiParam(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParam(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class")
    ])
    def update() {
        update(descriptionService, request.JSON)
    }

    /**
     * Delete a description
     */
    @ApiMethodLight(description="Delete a description")
    @ApiParams(params=[
        @ApiParam(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParam(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class")
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

