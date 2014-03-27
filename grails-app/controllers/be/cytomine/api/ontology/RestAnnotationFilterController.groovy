package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller that handle request on annotation filter
 */
@Api(name = "annotation filter services", description = "Methods for managing a filter for annotation search (save search criteria)")
class RestAnnotationFilterController extends RestController {

    def annotationFilterService
    def projectService
    def cytomineService

    /**
     * List all filter for a project
     */
    @ApiMethodLight(description="Get all annotation filters available for a specific project ", listing=true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="project", type="long", paramType = ApiParamType.PATH,description = "The project id"),
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
    @ApiMethodLight(description="Get all annotation filters available for an ontology ", listing=true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="idOntology", type="long", paramType = ApiParamType.PATH,description = "The ontology id"),
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
    @ApiMethodLight(description="Get an annotation filter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The filter id")
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
    @ApiMethodLight(description="Add a filter")
    def add() {
        def json= request.JSON
        json.user = springSecurityService.principal.id
        add(annotationFilterService, json)
    }

    /**
     * Update an annotation filter
     */
    @ApiMethodLight(description="Update a filter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The filter id")
    ])
    def update() {
        update(annotationFilterService, request.JSON)
    }

    /**
     * Delete an annotation filter
     */
    @ApiMethodLight(description="Delete a filter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The filter id")
    ])
    def delete() {
        delete(annotationFilterService,  JSON.parse("{id : $params.id}"),null)
    }

}
