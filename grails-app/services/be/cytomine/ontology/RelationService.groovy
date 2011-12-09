package be.cytomine.ontology

import be.cytomine.ModelService

class RelationService extends ModelService {

    static transactional = true

    def list() {
        Relation.list()
    }

    def read(def id) {
        Relation.read(id)
    }

    def readByName(String name) {
        Relation.findByName(name)
    }

    def getRelationParent() {
        readByName(RelationTerm.names.PARENT)
    }

    def add(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def update(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def delete(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
