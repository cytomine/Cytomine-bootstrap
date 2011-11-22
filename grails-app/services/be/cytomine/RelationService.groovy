package be.cytomine

import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm

class RelationService {

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
