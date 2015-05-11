package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.project.Project
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller that handle the link between a project and a image filter
 */
@RestApi(name = "image filter project services", description = "Methods for managing image filter project, a link between an image filter and a project")
class RestImageFilterProjectController extends RestController {

    def imageFilterProjectService
    def projectService
    def cytomineService

    /**
     * List all image filter project
     */
    @RestApiMethod(description="List all image filter project", listing = true)
    def list() {
 		responseSuccess(imageFilterProjectService.list())
    }

    /**
     * List all image filter for a project
     */
    @RestApiMethod(description="List all image filter project for a specific project", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
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
    @RestApiMethod(description="Add an image filter to a project")
    def add () {
        add(imageFilterProjectService, request.JSON)
    }

    /**
     * Delete an image filter from a project
     */
    @RestApiMethod(description="Delete an image filter from a project")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image filter id")
    ])
    def delete() {
        delete(imageFilterProjectService, JSON.parse("{id : $params.id}"),null)
    }

}
