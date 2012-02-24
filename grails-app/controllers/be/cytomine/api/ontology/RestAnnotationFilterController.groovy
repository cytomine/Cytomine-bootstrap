package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.project.Project
import be.cytomine.ontology.AnnotationFilter
import grails.converters.JSON
import be.cytomine.Exception.WrongArgumentException

class RestAnnotationFilterController extends RestController {

    def annotationFilterService
    def projectService
    def cytomineService

    def listByProject = {
        if (!params.long('project')) responseNotFound("Project", "undefined")
        Long idProject = params.long('project');
        projectService.checkAuthorization(idProject, new AnnotationFilter())
        Project project = projectService.read(idProject)
        responseSuccess(annotationFilterService.listByProject(project))
    }

    def show = {
        AnnotationFilter annotationFilter = annotationFilterService.read(params.id)
        if (!annotationFilter) responseNotFound("AnnotationFilter", params.id)
        projectService.checkAuthorization(annotationFilter.project)
        responseSuccess(annotationFilter)
    }

    def add = {
        def json= request.JSON
        json.user = springSecurityService.principal.id
        if(!json.project || !Project.read(json.project)) throw new WrongArgumentException("Annotation filter must have a valid project:"+json.project)
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
