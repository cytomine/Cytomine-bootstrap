package be.cytomine

import be.cytomine.api.RestController
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService

    @Secured(['ROLE_ADMIN'])
    def index() {
      //don't remove this, it calls admin/index.gsp layout !
    }

    @Secured(['ROLE_ADMIN'])
    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])
    }

}
