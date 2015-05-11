package be.cytomine

import be.cytomine.api.RestController
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
class AdminController extends RestController {


    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService

    @Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def index() {
      //don't remove this, it calls admin/index.gsp layout !
    }

    @Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])

    }


}
