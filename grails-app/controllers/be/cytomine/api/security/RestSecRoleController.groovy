package be.cytomine.api.security

import be.cytomine.api.RestController
import grails.plugins.springsecurity.Secured

/**
 * Controller for user roles
 * A user may have some roles (user, admin,...)
 */
class RestSecRoleController extends RestController {

    def secRoleService

    /**
     * List all roles available on cytomine
     */
    @Secured(['ROLE_ADMIN'])
    def list = {
        responseSuccess(secRoleService.list())
    }
}
