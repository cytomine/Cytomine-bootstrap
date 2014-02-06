package be.cytomine.api.security

import be.cytomine.api.RestController
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for user roles
 * A user may have some roles (user, admin,...)
 */
@Api(name = "sec role services", description = "Methods for managing user role")
class RestSecRoleController extends RestController {

    def secRoleService

    /**
     * List all roles available on cytomine
     */
    @ApiMethodLight(description="List all roles available on cytomine", listing=true)
    def list() {
        responseSuccess(secRoleService.list())
    }

    @ApiMethodLight(description="Get a role")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The role id")
    ])
    def show() {
        responseSuccess(secRoleService.read(params.id))
    }
}
