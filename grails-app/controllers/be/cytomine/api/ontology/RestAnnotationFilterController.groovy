package be.cytomine.api.ontology

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Controller that handle request on annotation filter
 */
class RestAnnotationFilterController extends RestController {

    def annotationFilterService
    def projectService
    def cytomineService

    /**
     * List all filter for a project
     */
    def listByProject = {
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
    def listByOntology = {
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
    def show = {
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
    def add = {
        def json= request.JSON
        json.user = springSecurityService.principal.id
        if(!json.project || !Project.read(json.project)) {
            throw new WrongArgumentException("Annotation filter must have a valid project:"+json.project)
        }
        add(annotationFilterService, json)
    }

    /**
     * Update an annotation filter
     */
    def update = {
        update(annotationFilterService, request.JSON)
    }

    /**
     * Delete an annotation filter
     */
    def delete = {
        delete(annotationFilterService,  JSON.parse("{id : $params.id}"))
    }

}
