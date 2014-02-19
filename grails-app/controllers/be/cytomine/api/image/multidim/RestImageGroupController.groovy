package be.cytomine.api.image.multidim

import be.cytomine.api.RestController
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 */
@Api(name = "image group services", description = "Methods for managing image group, a group of image from the same sample in different dimension (channel, zstack,...)")
class RestImageGroupController extends RestController {

    def imageGroupService
    def projectService

    @ApiMethodLight(description="Get an image group")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image group id")
    ])
    def show() {
        ImageGroup image = imageGroupService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageGroup", params.id)
        }
    }

    @ApiMethodLight(description="Get image group listing by project", listing=true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
        Project project = projectService.read(params.long('id'))

        if (project)  {
            responseSuccess(imageGroupService.list(project))
        }
        else {
            responseNotFound("ImageGroup", "Project", params.id)
        }
    }

    @ApiMethodLight(description="Add a new image group")
    def add () {
        add(imageGroupService, request.JSON)
    }

    @ApiMethodLight(description="Update an image group")
    @ApiParams(params=[
        @ApiParam(name="id", type="int", paramType = ApiParamType.PATH, description = "The image group id")
    ])
    def update() {
        update(imageGroupService, request.JSON)
    }

    @ApiMethodLight(description="Delete an image group")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image group")
    ])
    def delete() {
        delete(imageGroupService, JSON.parse("{id : $params.id}"),null)
    }
}
