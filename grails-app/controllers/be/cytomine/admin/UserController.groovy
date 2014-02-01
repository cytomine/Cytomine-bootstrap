package be.cytomine.admin

import be.cytomine.security.User
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN'])
class UserController {

    def scaffold = User
}
