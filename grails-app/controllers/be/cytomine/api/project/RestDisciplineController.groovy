package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Discipline
import grails.converters.JSON
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiBodyObject
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
 * Controller for discipline
 * A discipline can be link with a project
 */
@Api(name = "discipline services", description = "Methods for managing discipline")
class RestDisciplineController extends RestController {

    def disciplineService

    /**
     * List all discipline
     */
    @ApiMethod(
            path="/discipline.json",
            verb=ApiVerb.GET,
            description="Get discipline listing, according to your access",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponseObject(objectIdentifier = "discipline", multiple = "true")
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    ])
    def list () {
        responseSuccess(disciplineService.list())
    }

    /**
     * Get a single discipline
     */
    @ApiMethod(
            path="/discipline/{id}.json",
            verb=ApiVerb.GET,
            description="Get a discipline",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiParams(params=[
    @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    @ApiResponseObject(objectIdentifier = "discipline", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    @ApiError(code="404", description="Not found")
    ])
    def show () {
        Discipline discipline = disciplineService.read(params.long('id'))
        if (discipline) {
            responseSuccess(discipline)
        } else {
            responseNotFound("Discipline", params.id)
        }
    }

    /**
     * Add a new discipline
     */
    @ApiMethod(
            path="/discipline.json",
            verb=ApiVerb.POST,
            description="Add a new discipline",
            produces=[MediaType.APPLICATION_JSON_VALUE],
            consumes=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiBodyObject(name="discipline")
    @ApiResponseObject(objectIdentifier = "discipline", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="400", description="Bad Request"),
    @ApiError(code="401", description="Forbidden")
    ])
    def add () {
        add(disciplineService, request.JSON)
    }

    /**
     * Update a existing discipline
     */
    @ApiMethod(
            path="/discipline/{id}.json",
            verb=ApiVerb.PUT,
            description="Update a discipline",
            produces=[MediaType.APPLICATION_JSON_VALUE],
            consumes=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiParams(params=[
    @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    @ApiBodyObject(name="discipline")
    @ApiResponseObject(objectIdentifier = "discipline", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="400", description="Bad Request"),
    @ApiError(code="401", description="Forbidden"),
    @ApiError(code="404", description="Not found")
    ])
    def update () {
        update(disciplineService, request.JSON)
    }

    /**
     * Delete discipline
     */
    @ApiMethod(
            path="/discipline/{id}.json",
            verb=ApiVerb.DELETE,
            description="Delete a discipline",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiParams(params=[
    @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    @ApiError(code="404", description="Not found")
    ])
    def delete () {
        delete(disciplineService, JSON.parse("{id : $params.id}"),null)
    }

}
