package be.cytomine.ontology

import be.cytomine.utils.ModelService

/**
 * No security restriction for this domain (only read)
 */
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
}
