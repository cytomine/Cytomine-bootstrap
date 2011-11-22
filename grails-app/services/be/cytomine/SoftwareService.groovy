package be.cytomine

import be.cytomine.processing.Software

class SoftwareService {

    static transactional = true

    def list() {
        Software.list()
    }

    def read(def id) {
        Software.read(id)
    }
}
