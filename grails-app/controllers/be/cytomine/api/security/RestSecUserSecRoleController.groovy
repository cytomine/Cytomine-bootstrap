package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller to manage user role
 */
@Api(name = "sec user sec role services", description = "Methods for managing a user role")
class RestSecUserSecRoleController extends RestController {

    def secUserService
    def secRoleService
    def secUserSecRoleService
    def cytomineService

    /**
     * List all roles for a user
     */
    @ApiMethodLight(description="List all roles for a user", listing = true)
    @ApiParams(params=[
        @ApiParam(name="user", type="string", paramType = ApiParamType.PATH, description = "The user id")
    ])
    def list() {
        User user = secUserService.read(params.long('user'));
        responseSuccess(secUserSecRoleService.list(user))
    }

    /**
     * Check a role for a user
     * If user has not this role, send 404
     */
    @ApiMethodLight(description="Get a group")
    @ApiParams(params=[
        @ApiParam(name="user", type="string", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParam(name="role", type="string", paramType = ApiParamType.PATH, description = "The role id")
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
    @ApiMethodLight(description="Get a group")
    @ApiParams(params=[
        @ApiParam(name="user", type="string", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParam(name="role", type="string", paramType = ApiParamType.PATH, description = "The role id")
    ])
    def add() {
        add(secUserSecRoleService, request.JSON)
    }

    /**
     * Delete a role from a user
     */
    @ApiMethodLight(description="Delete a group")
    @ApiParams(params=[
        @ApiParam(name="user", type="string", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParam(name="role", type="string", paramType = ApiParamType.PATH, description = "The role id")
    ])
    def delete() {
        delete(secUserSecRoleService, JSON.parse("{user : $params.user, role: $params.role}"),null)
    }

}
