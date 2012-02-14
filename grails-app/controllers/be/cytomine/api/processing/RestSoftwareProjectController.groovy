package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.processing.SoftwareProject

class RestSoftwareProjectController extends RestController{

    def softwareProjectService


    def list = {
        responseSuccess(softwareProjectService.list())
    }

    def listBySoftware = {
        Software software = Software.read(params.long('id'))
        if(software) responseSuccess(softwareProjectService.list(software))
        else responseNotFound("Software", params.id)
    }

    def listByProject = {
        Project project = Project.read(params.long('id'))
        if(project) responseSuccess(softwareProjectService.list(project))
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
