package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.utils.Task
import grails.converters.JSON
import org.jsondoc.core.annotation.*
import org.jsondoc.core.pojo.ApiParamType
import org.jsondoc.core.pojo.ApiVerb
import org.springframework.http.MediaType

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
    @ApiMethod(
            path="/ontology.json",
            verb=ApiVerb.GET,
            description="Get ontologies listing, according to your access",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponseObject(objectIdentifier = "ontology", multiple = "true")
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    ])
    def list () {
        if(params.boolean("light")) {
            responseSuccess(ontologyService.listLight())
        } else {
            responseSuccess(ontologyService.list())
        }
    }

    @ApiMethod(
            path="/ontology/{id}.json",
            verb=ApiVerb.GET,
            description="Get an ontology",
            produces=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiParams(params=[
    @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    @ApiResponseObject(objectIdentifier = "ontology", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="401", description="Forbidden"),
    @ApiError(code="404", description="Not found")
    ])
    def show () {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess(ontology)
        } else {
            responseNotFound("Ontology", params.id)
        }
    }

    @ApiMethod(
            path="/ontology.json",
            verb=ApiVerb.POST,
            description="Add a new ontology",
            produces=[MediaType.APPLICATION_JSON_VALUE],
            consumes=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiBodyObject(name="ontology")
    @ApiResponseObject(objectIdentifier = "ontology", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="400", description="Bad Request"),
    @ApiError(code="401", description="Forbidden")
    ])
    def add () {
        add(ontologyService, request.JSON)
    }

    @ApiMethod(
            path="/ontology/{id}.json",
            verb=ApiVerb.PUT,
            description="Update an ontology",
            produces=[MediaType.APPLICATION_JSON_VALUE],
            consumes=[MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiParams(params=[
    @ApiParam(name="id", type="int", paramType = ApiParamType.PATH)
    ])
    @ApiBodyObject(name="ontology")
    @ApiResponseObject(objectIdentifier = "ontology", multiple = "false")
    @ApiErrors(apierrors=[
    @ApiError(code="400", description="Bad Request"),
    @ApiError(code="401", description="Forbidden"),
    @ApiError(code="404", description="Not found")
    ])
    def update () {
        update(ontologyService, request.JSON)
    }

    @ApiMethod(
            path="/ontology/{id}.json",
            verb=ApiVerb.DELETE,
            description="Delete an ontology",
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
        Task task = taskService.read(params.getLong("task"))
        delete(ontologyService, JSON.parse("{id : $params.id}"),task)
    }
}
