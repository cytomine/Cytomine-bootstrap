package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Project
import be.cytomine.project.ProjectDefaultLayer
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for project default layer
 */
@RestApi(name = "RestProjectDefaultLayerController", description = "Controller for project default layer")
class RestProjectDefaultLayerController extends RestController {

    def projectDefaultLayerService
    def taskService

    /**
     * List all default layers of a project
     */
    @RestApiMethod(description="List all default layers of a project", listing=true)
    @RestApiParams(params=[
            @RestApiParam(name="idProject", type="long", paramType = RestApiParamType.PATH, description = "The id of project")
    ])
    def listByProject() {
        Project project = Project.read(params.idProject)
        responseSuccess(projectDefaultLayerService.listByProject(project))
    }

    @RestApiMethod(description="Get a default layer")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The default layer id")
    ])
    def show () {
        ProjectDefaultLayer layer = projectDefaultLayerService.read(params.long('id'))
        if (layer) {
            responseSuccess(layer)
        } else {
            responseNotFound("ProjectDefaultLayer", params.id)
        }
    }

    @RestApiMethod(description="Add a default layer")
    def add () {
        add(projectDefaultLayerService, request.JSON)
    }

    @RestApiMethod(description="Update a default layer")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The default layer id")
    ])
    def update () {
        update(projectDefaultLayerService, request.JSON)
    }

    @RestApiMethod(description="Delete an default layer")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The default layer id"),
            @RestApiParam(name="task", type="long", paramType = RestApiParamType.PATH,description = "(Optional, default:null) The id of the task to update during process"),
    ])
    def delete () {
        Task task = taskService.read(params.getLong("task"))
        delete(projectDefaultLayerService, JSON.parse("{id : $params.id}"),task)
    }
}
