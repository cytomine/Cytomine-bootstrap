package be.cytomine.ontology
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

      returnArray['data'] = it.name
      returnArray['state'] = "open"

      def terms = []
      try {
      Term.findAllByOntology(it).each {
          def term = [:]
          term.id = it.getId()
          term.text = it.getName()
          term.data = it.getName()
          term.checked = false
          term.leaf = false
          terms << term
      }
      }   //TODO: Term.findAllByOntology(it) throw exception if Ontology (it) is not save before...Must be change!
      catch(Exception e) {println "ERROR: " + e}
      returnArray['children'] = terms

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
