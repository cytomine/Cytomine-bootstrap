package be.cytomine.api.security

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.api.RestController
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
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
    @RestApiMethod(description="Get a user role")
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
    @RestApiMethod(description="Get a user role")
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
    @RestApiMethod(description="Delete a user role")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="string", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="role", type="string", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def delete() {
        delete(secUserSecRoleService, JSON.parse("{user : $params.user, role: $params.role}"),null)
    }

    @RestApiMethod(description="Define a role for a user. If admin is defined, user will have admin,user,guest. If user is defined, user will have user,guest, etc. Role may be create or remove")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="string", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="role", type="string", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def define() {
        SecUser user = SecUser.read(params.long('user'))
        SecRole role = SecRole.read(params.long('role'))

        try {
            if(!user) {
                throw new ObjectNotFoundException("Cannot read user ${params.long('user')}")
            }
            if(!role) {
                throw new ObjectNotFoundException("Cannot read role${params.long('role')}")
            }
            secUserSecRoleService.define(user,role)

            responseSuccess(user.authorities)

        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

}
