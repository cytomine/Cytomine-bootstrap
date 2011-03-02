package be.cytomine.project
import grails.converters.JSON
class Ontology {

  String name

  static constraints = {
    name(blank:false, unique:true)
  }

  def terms() {
      Term.findAllByOntology(this)
  }

  def termsParent() {
      Term.findAllByOntology(this)
    //TODO: Check RelationTerm to remove term which have parents
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Ontology.class
    JSON.registerObjectMarshaller(Ontology) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['children'] = it.termsParent()
      return returnArray
    }
  }
}
