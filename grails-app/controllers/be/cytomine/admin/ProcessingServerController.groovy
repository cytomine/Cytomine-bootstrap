package be.cytomine.admin

import be.cytomine.image.server.ProcessingServer
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class ProcessingServerController {

    def scaffold = ProcessingServer
}
