package be.cytomine.api.security

import be.cytomine.api.RestController
import grails.plugins.springsecurity.Secured

class RestSecRoleController extends RestController {

    def secRoleService

    @Secured(['ROLE_ADMIN'])
    def list = {
        responseSuccess(secRoleService.list())
    }
}
