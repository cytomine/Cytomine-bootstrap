package be.cytomine.image.server

import be.cytomine.processing.ProcessingServer
import grails.plugins.springsecurity.Secured

class ProcessingServerService {

    @Secured(['ROLE_USER'])
    def list() {
        ProcessingServer.list()
    }

    @Secured(['ROLE_USER'])
    def read(long id) {
        return ProcessingServer.read(id)
    }

}
