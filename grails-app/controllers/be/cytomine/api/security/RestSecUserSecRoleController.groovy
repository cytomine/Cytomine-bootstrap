package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller to manage user role
 */
@RestApi(name = "sec user sec role services", description = "Methods for managing a user role")
class RestSecUserSecRoleController extends RestController {

    def secUserService
    def secRoleService
    def secUserSecRoleService
    def cytomineService

    /**
     * List all roles for a user
     */
    @RestApiMethod(description="List all roles for a user", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="user", type="string", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    def list() {
        User user = secUserService.read(params.long('user'));
        responseSuccess(secUserSecRoleService.list(user))
    }

    /**
     * Check a role for a user
     * If user has not this role, send 404
     */
    @RestApiMethod(description="Get a group")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="string", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="role", type="string", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def show() {
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
    @RestApiMethod(description="Get a group")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="string", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="role", type="string", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def add() {
        add(secUserSecRoleService, request.JSON)
    }

    /**
     * Delete a role from a user
     */
    @RestApiMethod(description="Delete a group")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="string", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="role", type="string", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def delete() {
        delete(secUserSecRoleService, JSON.parse("{user : $params.user, role: $params.role}"),null)
    }

}
