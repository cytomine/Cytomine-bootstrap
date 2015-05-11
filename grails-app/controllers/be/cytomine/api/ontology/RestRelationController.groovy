package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for relation between terms (parent, synonym,...)
 * We only use "parent" now, but we could later implement CRUD to support new type of relation
 */
@RestApi(name = "relation services", description = "Methods for managing relations")
class RestRelationController extends RestController {


    def springSecurityService
    def relationService

    /**
     * List all relation available
     */
    @RestApiMethod(description="List all relation available", listing = true)
    def list () {
        responseSuccess(relationService.list())
    }

    /**
     * Get a single relation with its id
     */
    @RestApiMethod(description="Get a relation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The relation id")
    ])
    def show () {
        Relation relation = relationService.read(params.long('id'))
        if (relation) {
            responseSuccess(relation)
        } else {
            responseNotFound("Relation", params.id)
        }
    }
}
