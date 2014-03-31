package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for ontology (terms tree)
 */
@RestApi(name = "ontology services", description = "Methods for managing ontologies")
class RestOntologyController extends RestController {

    def springSecurityService
    def ontologyService
    def cytomineService
    def termService
    def taskService

    /**
     * List all ontology visible for the current user
     * For each ontology, print the terms tree
     */
    @RestApiMethod(description="Get all ontologies available", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="light", type="boolean", paramType = RestApiParamType.QUERY,description = "(Optional, default false) Only get a light list (with no term tree for each ontologies)")
    ])
    def list () {
        if(params.boolean("light")) {
            responseSuccess(ontologyService.listLight())
        } else {
            responseSuccess(ontologyService.list())
        }
    }

    @RestApiMethod(description="Get an ontology")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The ontology id")
    ])
    def show () {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess(ontology)
        } else {
            responseNotFound("Ontology", params.id)
        }
    }

    @RestApiMethod(description="Add an ontology")
    def add () {
        add(ontologyService, request.JSON)
    }

    @RestApiMethod(description="Update an ontology")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The ontology id")
    ])
    def update () {
        update(ontologyService, request.JSON)
    }

    @RestApiMethod(description="Delete an ontology")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The ontology id"),
        @RestApiParam(name="task", type="long", paramType = RestApiParamType.PATH,description = "(Optional, default:null) The id of the task to update during process"),
    ])
    def delete () {
        Task task = taskService.read(params.getLong("task"))
        delete(ontologyService, JSON.parse("{id : $params.id}"),task)
    }
}
