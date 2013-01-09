package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

/**
 * Controller to manage user in group
 */
class RestUserGroupController extends RestController {

    def userGroupService
    def userService
    def groupService
    def transactionService
    def cytomineService

    /**
     * List all user-group for a user
     */
    @Secured(['ROLE_ADMIN'])
    def list = {
        User user = userService.read(params.long('user'));
        responseSuccess(userGroupService.list(user))
    }

    /**
     * Get a user-group relation
     */
    @Secured(['ROLE_ADMIN'])
    def show = {
        User user = userService.read(params.long('user'));
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
