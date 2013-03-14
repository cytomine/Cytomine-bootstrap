package be.cytomine

import be.cytomine.api.RestController
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication

    def index() {
    }

}
