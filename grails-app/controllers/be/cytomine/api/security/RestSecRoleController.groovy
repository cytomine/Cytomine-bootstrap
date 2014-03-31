package be.cytomine.api.security

import be.cytomine.api.RestController
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for user roles
 * A user may have some roles (user, admin,...)
 */
@RestApi(name = "sec role services", description = "Methods for managing user role")
class RestSecRoleController extends RestController {

    def secRoleService

    /**
     * List all roles available on cytomine
     */
    @RestApiMethod(description="List all roles available on cytomine", listing=true)
    def list() {
        responseSuccess(secRoleService.list())
    }

    @RestApiMethod(description="Get a role")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def show() {
        responseSuccess(secRoleService.read(params.id))
    }
}
