package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import grails.converters.JSON

class RestSoftwareProjectController extends RestController{

    def softwareProjectService
    def projectService

    def list = {
        responseSuccess(softwareProjectService.list())
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) responseSuccess(softwareProjectService.list(project))
        else responseNotFound("Project", params.id)
    }

    def show = {
        SoftwareProject parameter = softwareProjectService.read(params.long('id'))
        if (parameter) responseSuccess(parameter)
        else responseNotFound("SoftwareProject", params.id)
    }

    def add = {
        add(softwareProjectService, request.JSON)
    }

    def delete = {
        delete(softwareProjectService, JSON.parse("{id : $params.id}"))
    }
}
