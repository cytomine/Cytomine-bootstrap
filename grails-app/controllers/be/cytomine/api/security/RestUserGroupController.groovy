package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller to manage user in group
 */
@Api(name = "user group services", description = "Methods for managing a user in groups")
class RestUserGroupController extends RestController {

    def userGroupService
    def secUserService
    def groupService
    def cytomineService

    /**
     * List all user-group for a user
     */
    @ApiMethodLight(description="List all user group for a user", listing = true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="user", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    def list() {
        User user = secUserService.read(params.long('user'));
        responseSuccess(userGroupService.list(user))
    }

    /**
     * Get a user-group relation
     */
    @ApiMethodLight(description="Get a user-group relation")
    @ApiParamsLight(params=[
        @ApiParamLight(name="user", type="long", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParamLight(name="group", type="long", paramType = ApiParamType.PATH, description = "The group id")
    ])
    def show() {
        User user = secUserService.read(params.long('user'));
        Group group = groupService.read(params.long('group'));
        UserGroup userGroup = userGroupService.get(user, group)
        if (!userGroup) {
            responseNotFound("UserGroup", params.user)
        } else {
            responseSuccess(userGroup)
        }
    }

    /**
     * Add a new user to a group
     */
    @ApiMethodLight(description="Get a user-group relation")
    def add() {
        add(userGroupService, request.JSON)
    }

    /**
     * Remove a user from a group
     */
    @ApiMethodLight(description="Remove a user from a group")
    @ApiParamsLight(params=[
        @ApiParamLight(name="user", type="long", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParamLight(name="group", type="long", paramType = ApiParamType.PATH, description = "The group id")
    ])
    def delete() {
        def json = JSON.parse("{user : $params.user, group: $params.group}")
        delete(userGroupService, json,null)
    }
}
