package be.cytomine.image.server

import be.cytomine.SecurityACL
import be.cytomine.processing.ProcessingServer

class ProcessingServerService {

    def cytomineService

    def list() {
        SecurityACL.checkGhest(cytomineService.currentUser)
        ProcessingServer.list()
    }

    def read(long id) {
        SecurityACL.checkGhest(cytomineService.currentUser)
        return ProcessingServer.read(id)
    }

}
