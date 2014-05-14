package be.cytomine.image.server


import be.cytomine.processing.ProcessingServer

class ProcessingServerService {

    def cytomineService
    def securityACLService

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        ProcessingServer.list()
    }

    def read(long id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        return ProcessingServer.read(id)
    }

}
