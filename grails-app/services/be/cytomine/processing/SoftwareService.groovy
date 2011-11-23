package be.cytomine.processing

import be.cytomine.ModelService

class SoftwareService extends ModelService {

    static transactional = true

    def list() {
        Software.list()
    }

    def read(def id) {
        Software.read(id)
    }

    def add(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def delete(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
