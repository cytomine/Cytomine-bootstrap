package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Controller that handle the link between a project and a image filter
 */
class RestImageFilterProjectController extends RestController {

    def imageFilterProjectService
    def projectService
    def cytomineService

    /**
     * List all image filter project
     */
    def list = {
 		responseSuccess(imageFilterProjectService.list())
    }

    /**
     * List all image filter for a project
     */
    def listByProject = {
        def project = Project.read(params.project)
		if (!project) {
            responseNotFound("Project", "Project", params.project)
            return
        }
        def imagesFiltersProject = imageFilterProjectService.list(project)
 		responseSuccess(imagesFiltersProject)
    }

    /**
     * Add an image filter to a project
     */
    def add = {
        add(imageFilterProjectService, request.JSON)
    }

    /**
     * Delete an image filter from a project
     */
    def delete = {
        delete(imageFilterProjectService, JSON.parse("{id : $params.id}"),null)
    }

}
