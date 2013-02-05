package be.cytomine.ontology

import be.cytomine.utils.ModelService
import be.cytomine.project.Project
import be.cytomine.command.Transaction
//import be.cytomine.Exception.CytomineMethodNotYetImplementedException
import be.cytomine.command.Task

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

    def deleteDependentRelationTerm(Project project, Transaction transaction, Task task = null) {
        //throw new CytomineMethodNotYetImplementedException("");
    }
}
