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
      return returnArray
    }
  }

  static Ontology createOntologyFromData(jsonOntology) {
    def ontology = new Ontology()
    getOntologyFromData(ontology,jsonOntology)
  }

  static Ontology getOntologyFromData(ontology,jsonOntology) {
    if(!jsonOntology.name.toString().equals("null"))
      ontology.name = jsonOntology.name
    else throw new IllegalArgumentException("Ontology name cannot be null")

    return ontology;
  }

}
