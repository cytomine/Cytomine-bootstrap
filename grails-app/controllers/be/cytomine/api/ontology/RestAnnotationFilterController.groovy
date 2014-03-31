package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller that handle request on annotation filter
 */
@RestApi(name = "annotation filter services", description = "Methods for managing a filter for annotation search (save search criteria)")
class RestAnnotationFilterController extends RestController {

    def annotationFilterService
    def projectService
    def cytomineService

    /**
     * List all filter for a project
     */
    @RestApiMethod(description="Get all annotation filters available for a specific project ", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
    ])
    def listByProject() {
        Long idProject = params.long('project');
        Project project = projectService.read(idProject)
        if(project) {
            responseSuccess(annotationFilterService.listByProject(project))
        } else {
            responseNotFound("Project", params.project)
        }
    }

    /**
     * List all filter for an ontology
     */
    @RestApiMethod(description="Get all annotation filters available for an ontology ", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="idOntology", type="long", paramType = RestApiParamType.PATH,description = "The ontology id"),
    ])
    def listByOntology() {
        Ontology ontology = Ontology.read(params.idOntology)
        if (ontology) {
            def result = []
            List<Project> userProject = projectService.list(ontology)
            userProject.each {
                result.addAll(annotationFilterService.listByProject(it))
            }
            responseSuccess(result)
        } else {
            responseNotFound("ImageFilter", "Ontology", params.idOntology)
        }
    }

    /**
     * Get an annotation filter
     */
    @RestApiMethod(description="Get an annotation filter")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The filter id")
    ])
    def show() {
        AnnotationFilter annotationFilter = annotationFilterService.read(params.id)
        if (!annotationFilter) {
            responseNotFound("AnnotationFilter", params.id)
        } else {
            responseSuccess(annotationFilter)
        }
    }

    /**
     * Add a new annotation filter
     */
    @RestApiMethod(description="Add a filter")
    def add() {
        def json= request.JSON
        json.user = springSecurityService.principal.id
        add(annotationFilterService, json)
    }

    /**
     * Update an annotation filter
     */
    @RestApiMethod(description="Update a filter")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The filter id")
    ])
    def update() {
        update(annotationFilterService, request.JSON)
    }

    /**
     * Delete an annotation filter
     */
    @RestApiMethod(description="Delete a filter")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The filter id")
    ])
    def delete() {
        delete(annotationFilterService,  JSON.parse("{id : $params.id}"),null)
    }

}
