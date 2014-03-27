package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.utils.Task
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for ontology (terms tree)
 */
@Api(name = "ontology services", description = "Methods for managing ontologies")
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
    @ApiMethodLight(description="Get all ontologies available", listing=true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="light", type="boolean", paramType = ApiParamType.QUERY,description = "(Optional, default false) Only get a light list (with no term tree for each ontologies)")
    ])
    def list () {
        if(params.boolean("light")) {
            responseSuccess(ontologyService.listLight())
        } else {
            responseSuccess(ontologyService.list())
        }
    }

    @ApiMethodLight(description="Get an ontology")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The ontology id")
    ])
    def show () {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess(ontology)
        } else {
            responseNotFound("Ontology", params.id)
        }
    }

    @ApiMethodLight(description="Add an ontology")
    def add () {
        add(ontologyService, request.JSON)
    }

    @ApiMethodLight(description="Update an ontology")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The ontology id")
    ])
    def update () {
        update(ontologyService, request.JSON)
    }

    @ApiMethodLight(description="Delete an ontology")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The ontology id"),
        @ApiParamLight(name="task", type="long", paramType = ApiParamType.PATH,description = "(Optional, default:null) The id of the task to update during process"),
    ])
    def delete () {
        Task task = taskService.read(params.getLong("task"))
        delete(ontologyService, JSON.parse("{id : $params.id}"),task)
    }
}
