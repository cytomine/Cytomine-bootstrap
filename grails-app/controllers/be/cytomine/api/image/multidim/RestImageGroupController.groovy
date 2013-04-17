package be.cytomine.api.image.multidim

import be.cytomine.api.RestController
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.project.Project
import grails.converters.JSON
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 */
class RestImageGroupController extends RestController {

    def imageGroupService
    def projectService

    def show = {
        ImageGroup image = imageGroupService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageGroup", params.id)
        }
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'))

        if (project)  {
            responseSuccess(imageGroupService.list(project))
        }
        else {
            responseNotFound("ImageGroup", "Project", params.id)
        }
    }

    def add = {
        add(imageGroupService, request.JSON)
    }

    def update = {
        update(imageGroupService, request.JSON)
    }

    def delete = {
        delete(imageGroupService, JSON.parse("{id : $params.id}"),null)
    }
}
