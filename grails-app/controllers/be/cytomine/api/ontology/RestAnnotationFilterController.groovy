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

    def listByProject = {
        if (!params.long('project')) {
            responseNotFound("Project", "undefined")
        }
        Long idProject = params.long('project');
        projectService.checkAuthorization(idProject, new AnnotationFilter())
        Project project = projectService.read(idProject, new Project())
        responseSuccess(annotationFilterService.listByProject(project))
    }

    def listByOntology = {
        Ontology ontology = Ontology.read(params.idOntology)
        if (ontology) {
            def result = []
            List<Project> userProject = projectService.list(ontology)

            userProject.each {
                result.addAll(annotationFilterService.listByProject(it))
            }

            responseSuccess(result)
        }
        else responseNotFound("ImageFilter", "Ontology", params.idOntology)
    }

    def show = {
        AnnotationFilter annotationFilter = annotationFilterService.read(params.id)
        if (!annotationFilter) {
            responseNotFound("AnnotationFilter", params.id)
        }
        projectService.checkAuthorization(annotationFilter.project)
        responseSuccess(annotationFilter)
    }

    def add = {
        def json= request.JSON
        json.user = springSecurityService.principal.id
        if(!json.project || !Project.read(json.project)) {
            throw new WrongArgumentException("Annotation filter must have a valid project:"+json.project)
        }
        projectService.checkAuthorization(Long.parseLong(json.project.toString()),new AnnotationFilter())
        add(annotationFilterService, json)
    }

    def update = {
        update(annotationFilterService, request.JSON)
    }

    def delete = {
        delete(annotationFilterService,  JSON.parse("{id : $params.id}"))
    }

}
