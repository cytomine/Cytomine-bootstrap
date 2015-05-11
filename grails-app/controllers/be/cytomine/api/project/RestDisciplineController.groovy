package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Discipline
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for discipline
 * A discipline can be link with a project
 */
@RestApi(name = "discipline services", description = "Methods for managing discipline")
class RestDisciplineController extends RestController {

    def disciplineService
    /**
     * List all discipline
     */
    @RestApiMethod(description="Get discipline listing, according to your access", listing = true)
    def list () {
        responseSuccess(disciplineService.list())
    }

    /**
     * Get a single discipline
     */
    @RestApiMethod(description="Get a discipline")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The discipline id")
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
    @RestApiMethod(description="Add a new discipline")
    def add () {
        add(disciplineService, request.JSON)
    }

    /**
     * Update a existing discipline
     */
    @RestApiMethod(description="Update a discipline")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="int", paramType = RestApiParamType.PATH)
    ])
    def update () {
        update(disciplineService, request.JSON)
    }

    /**
     * Delete discipline
     */
    @RestApiMethod(description="Delete a discipline")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="int", paramType = RestApiParamType.PATH)
    ])
    def delete () {
        delete(disciplineService, JSON.parse("{id : $params.id}"),null)
    }

}
