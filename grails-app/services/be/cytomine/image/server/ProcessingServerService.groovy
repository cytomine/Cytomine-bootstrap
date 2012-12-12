package be.cytomine.image.server

import grails.plugins.springsecurity.Secured

class ProcessingServerService {

    @Secured(['ROLE_USER'])
    def list() {
        ProcessingServer.list()
    }
}
