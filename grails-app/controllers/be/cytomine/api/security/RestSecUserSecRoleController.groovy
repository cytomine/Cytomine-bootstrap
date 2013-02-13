package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.converters.JSON

/**
 * Controller to manage user role
 */
class RestSecUserSecRoleController extends RestController {

    def secUserService
    def secRoleService
    def secUserSecRoleService
    def cytomineService
    def transactionService

    /**
     * List all roles for a user
     */
    def list = {
        User user = secUserService.read(params.long('user'));
        responseSuccess(secUserSecRoleService.list(user))
    }

    /**
     * Check a role for a user
     * If user has not this role, send 404
     */
    def show = {
        User user = secUserService.read(params.long('user'));
        SecRole role = secRoleService.read(params.long('role'));
        SecUserSecRole secUserSecRole = secUserSecRoleService.get(user, role)
        if (!secUserSecRole) {
            responseNotFound("SecUserSecRole", params.user)
        } else {
            responseSuccess(secUserSecRole)
        }
    }

    /**
     * Add a new role to a user
     */
    def add = {
        add(secUserSecRoleService, request.JSON)
    }

    /**
     * Delete a role from a user
     */
    def delete = {
        delete(secUserSecRoleService, JSON.parse("{user : $params.user, role: $params.role}"),null)
    }

}
