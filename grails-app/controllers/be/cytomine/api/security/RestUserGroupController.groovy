package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import grails.converters.JSON

/**
 * Controller to manage user in group
 */
class RestUserGroupController extends RestController {

    def userGroupService
    def secUserService
    def groupService
    def transactionService
    def cytomineService

    /**
     * List all user-group for a user
     */
    def list = {
        User user = secUserService.read(params.long('user'));
        responseSuccess(userGroupService.list(user))
    }

    /**
     * Get a user-group relation
     */
    def show = {
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
    def add = {
        add(userGroupService, request.JSON)
    }

    /**
     * Remove a user from a group
     */
    def delete = {
        def json = JSON.parse("{user : $params.user, group: $params.group}")
        delete(userGroupService, json)
    }
}
