package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for group of users
 */
@Api(name = "group services", description = "Methods for managing user groups")
class RestGroupController extends RestController {

    def abstractImageService
    def groupService

    /**
     * List all group
     */
    @ApiMethodLight(description="List all group", listing=true)
    def list() {
        responseSuccess(groupService.list())
    }

    /**
     * Get a group info
     */
    @ApiMethodLight(description="Get a group")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The group id")
    ])
    def show() {
        Group group = groupService.read(params.long('id'))
        if (group) {
            responseSuccess(group)
        } else {
            responseNotFound("Group", params.id)
        }
    }

    /**
     * Add a new group
     */
    @ApiMethodLight(description="Add a new group")
    def add() {
        add(groupService, request.JSON)
    }

    /**
     * Update a group
     */
    @ApiMethodLight(description="Edit a group")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The group id")
    ])
    def update() {
        update(groupService, request.JSON)
    }

    /**
     * Delete a group
     */
    @ApiMethodLight(description="Delete a group")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The group id")
    ])
    def delete() {
        delete(groupService, JSON.parse("{id : $params.id}"),null)
    }
}
