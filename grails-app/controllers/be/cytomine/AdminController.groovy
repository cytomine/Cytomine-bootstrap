package be.cytomine

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.test.BasicInstanceBuilder
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService
    def simplifyGeometryService
    def scriptService


    def index() {
      //don't remove this, it calls admin/index.gsp layout !
    }



    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])
    }



}
