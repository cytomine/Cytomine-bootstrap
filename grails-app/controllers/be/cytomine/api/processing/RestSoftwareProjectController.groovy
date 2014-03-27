package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for software project link
 * A software may be used by some project
 */
@Api(name = "software project services", description = "Methods for managing software, application that can be launch (job)")
class RestSoftwareProjectController extends RestController{

    def softwareProjectService
    def projectService

    /**
     * List all software project links
     */
    @ApiMethodLight(description="List all software project links", listing = true)
    def list() {
        responseSuccess(softwareProjectService.list())
    }

    /**
     * List all software by project
     */
    @ApiMethodLight(description="List all software project links by project", listing = true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
        Project project = projectService.read(params.long('id'))
        if (project) {
            responseSuccess(softwareProjectService.list(project))
        } else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * Get a software project link
     */
    @ApiMethodLight(description="Get a software project link")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The software project id")
    ])
    def show() {
        SoftwareProject parameter = softwareProjectService.read(params.long('id'))
        if (parameter) responseSuccess(parameter)
        else responseNotFound("SoftwareProject", params.id)
    }

    /**
     * Add an existing software to a project
     */
    @ApiMethodLight(description="Add an existing software to a project")
    def add () {
        add(softwareProjectService, request.JSON)
    }

    /**
     * Delete the software for the project
     */
    @ApiMethodLight(description="Remove the software from the project")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The software project id")
    ])
    def delete() {
        delete(softwareProjectService, JSON.parse("{id : $params.id}"),null)
    }
}
