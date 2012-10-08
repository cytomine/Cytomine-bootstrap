package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.project.Project
import grails.converters.JSON

class RestImageFilterProjectController extends RestController {

    def imageFilterService
    def imageFilterProjectService
    def projectService
    def cytomineService


    def list = {
 		responseSuccess(imageFilterProjectService.list())
    }

    def listByProject = {
         def project = Project.read(params.project)
		if (!project) responseNotFound("Project", "Project", params.project)
        def imagesFiltersProject = imageFilterProjectService.list(project)
 		responseSuccess(imagesFiltersProject)
    }


    def add = {
        add(imageFilterProjectService, request.JSON)
    }

    def delete = {
        log.info "DELETE imageFilterProjectService " + params.id
        delete(imageFilterProjectService, JSON.parse("{id : $params.id}"))
    }

}
