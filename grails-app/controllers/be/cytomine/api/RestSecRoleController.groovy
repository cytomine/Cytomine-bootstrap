package be.cytomine.api

import grails.plugins.springsecurity.Secured
import be.cytomine.security.SecRole
import be.cytomine.security.User

class RestSecRoleController extends RestController {

    @Secured(['ROLE_ADMIN'])
    def list = {
        responseSuccess(SecRole.list())
    }

}
