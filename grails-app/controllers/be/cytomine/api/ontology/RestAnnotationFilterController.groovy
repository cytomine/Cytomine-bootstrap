package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.project.Project
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.security.User

class RestAnnotationFilterController extends RestController {

    def annotationFilterService
    def projectService
    def cytomineService

    def list = {
        if (params.project) {
            Project project = projectService.read(params.project)
            if (!project) {
                responseNotFound("Project", params.project)
            }
            responseSuccess(annotationFilterService.listByProject(project))
        }
        responseSuccess(annotationFilterService.list())
    }

    def show = {
        AnnotationFilter annotationFilter = annotationFilterService.read(params.id)
        if (!annotationFilter) responseNotFound("AnnotationFilter", params.id)
        responseSuccess(annotationFilter)
    }

    def add = {
        add(annotationFilterService, request.JSON)
    }

    def update = {
        update(annotationFilterService, request.JSON)
    }

    def delete = {
        delete(annotationFilterService, request.JSON)
    }

}
