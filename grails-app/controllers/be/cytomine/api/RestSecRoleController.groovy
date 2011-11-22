package be.cytomine.api

import grails.plugins.springsecurity.Secured

class RestSecRoleController extends RestController {

    def secRoleService

    @Secured(['ROLE_ADMIN'])
    def list = {
        responseSuccess(secRoleService.list())
    }
}
