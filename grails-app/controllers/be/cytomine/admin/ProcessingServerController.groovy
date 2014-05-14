package be.cytomine.admin

import be.cytomine.processing.ProcessingServer
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
class ProcessingServerController {

    def scaffold = ProcessingServer
}
