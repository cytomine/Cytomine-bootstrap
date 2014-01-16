package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Discipline
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

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
    @ApiMethodLight(description="Get discipline listing, according to your access", listing = true)
    def list () {
        responseSuccess(disciplineService.list())
    }

    /**
     * Get a single discipline
     */
    @ApiMethodLight(description="Get a discipline")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The discipline id")
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
    @ApiMethodLight(description="Add a new discipline")
    def add () {
        add(disciplineService, request.JSON)
    }

    /**
     * Update a existing discipline
     */
    @ApiMethodLight(description="Update a discipline")
    @ApiParams(params=[
        @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    def update () {
        update(disciplineService, request.JSON)
    }

    /**
     * Delete discipline
     */
    @ApiMethodLight(description="Delete a discipline")
    @ApiParams(params=[
        @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    def delete () {
        delete(disciplineService, JSON.parse("{id : $params.id}"),null)
    }

}
