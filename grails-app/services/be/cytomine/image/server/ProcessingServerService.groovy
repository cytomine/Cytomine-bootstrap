package be.cytomine.image.server

import be.cytomine.SecurityACL
import be.cytomine.processing.ProcessingServer

class ProcessingServerService {

    def cytomineService

    def list() {
        SecurityACL.checkGuest(cytomineService.currentUser)
        ProcessingServer.list()
    }

    def read(long id) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        return ProcessingServer.read(id)
    }

}
