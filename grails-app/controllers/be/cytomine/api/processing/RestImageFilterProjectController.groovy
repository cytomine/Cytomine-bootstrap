package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller that handle the link between a project and a image filter
 */
@Api(name = "image filter project services", description = "Methods for managing image filter project, a link between an image filter and a project")
class RestImageFilterProjectController extends RestController {

    def imageFilterProjectService
    def projectService
    def cytomineService

    /**
     * List all image filter project
     */
    @ApiMethodLight(description="List all image filter project", listing = true)
    def list() {
 		responseSuccess(imageFilterProjectService.list())
    }

    /**
     * List all image filter for a project
     */
    @ApiMethodLight(description="List all image filter project for a specific project", listing=true)
    @ApiParams(params=[
        @ApiParam(name="project", type="long", paramType = ApiParamType.PATH, description = "The project id")
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
    @ApiMethodLight(description="Add an image filter to a project")
    def add () {
        add(imageFilterProjectService, request.JSON)
    }

    /**
     * Delete an image filter from a project
     */
    @ApiMethodLight(description="Delete an image filter from a project")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image filter id")
    ])
    def delete() {
        delete(imageFilterProjectService, JSON.parse("{id : $params.id}"),null)
    }

}
