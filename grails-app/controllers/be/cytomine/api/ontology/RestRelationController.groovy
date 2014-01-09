package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiError
import org.jsondoc.core.annotation.ApiErrors
import org.jsondoc.core.annotation.ApiMethod
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType
import org.jsondoc.core.pojo.ApiVerb
import org.springframework.http.MediaType

/**
 * Controller for relation between terms (parent, synonym,...)
 * We only use "parent" now, but we could later implement CRUD to support new type of relation
 */
@Api(name = "relation services", description = "Methods for managing relations")
class RestRelationController extends RestController {


    def springSecurityService
    def relationService

    /**
     * List all relation available
     */
    @ApiMethod(
            path="/relation.json",
            verb=ApiVerb.GET,
            description="Get relation types listing",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponseObject(objectIdentifier = "relation", multiple = "true")
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    ])
    def list () {
        responseSuccess(relationService.list())
    }

    /**
     * Get a single relation with its id
     */
    @ApiMethod(
            path="/relation/{id}.json",
            verb=ApiVerb.GET,
            description="Get a relation",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiParams(params=[
    @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    @ApiResponseObject(objectIdentifier = "relation", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    @ApiError(code="404", description="Not found")
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
