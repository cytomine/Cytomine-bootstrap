package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Controller for software project link
 * A software may be used by some project
 */
class RestSoftwareProjectController extends RestController{

    def softwareProjectService
    def projectService

    /**
     * List all software parameter links
     */
    def list = {
        responseSuccess(softwareProjectService.list())
    }

    /**
     * List all software parameter by project
     */
    def listByProject = {
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
    def show = {
        SoftwareProject parameter = softwareProjectService.read(params.long('id'))
        if (parameter) responseSuccess(parameter)
        else responseNotFound("SoftwareProject", params.id)
    }

    /**
     * Add a existing software to a project
     */
    def add = {
        add(softwareProjectService, request.JSON)
    }

    /**
     * Delete the software for the project
     */
    def delete = {
        delete(softwareProjectService, JSON.parse("{id : $params.id}"))
    }
}
