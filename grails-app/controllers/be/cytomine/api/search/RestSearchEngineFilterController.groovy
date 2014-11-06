package be.cytomine.api.search

import be.cytomine.api.RestController
import be.cytomine.search.SearchEngineFilter
import be.cytomine.search.SearchEngineFilterService
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for search engine filter
 */
@RestApi(name = "search engine filter services", description = "Methods for managing filter of the search engine")
class RestSearchEngineFilterController extends RestController {

    def springSecurityService
    def searchEngineFilterService
    def cytomineService
    def taskService

    /**
     * List all filters of the current user
     */
    @RestApiMethod(description="List all filters", listing=true)
    def list () {
        responseSuccess(searchEngineFilterService.list())
    }

    /**
     * List all filters of the current user
     */
    @RestApiMethod(description="List all filters of the current user", listing=true)
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    def listByUser () {
        responseSuccess(searchEngineFilterService.listByUser(params.long('id')))
    }

    @RestApiMethod(description="Get a filter")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The filter id")
    ])
    def show () {
        SearchEngineFilter filter = searchEngineFilterService.read(params.long('id'))
        if (filter) {
            responseSuccess(filter)
        } else {
            responseNotFound("Search Engine Filter", params.id)
        }
    }

    @RestApiMethod(description="Add an filter")
    def add () {
        add(searchEngineFilterService, request.JSON)
    }
    @RestApiMethod(description="Delete an filter")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The filter id"),
            @RestApiParam(name="task", type="long", paramType = RestApiParamType.PATH,description = "(Optional, default:null) The id of the task to update during process"),
    ])
    def delete () {
        Task task = taskService.read(params.getLong("task"))
        delete(searchEngineFilterService, JSON.parse("{id : $params.id}"),task)
    }
}
