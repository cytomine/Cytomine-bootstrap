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

  def tree () {
    def rootTerms = []
    this.terms().each {
      if (!it.isRoot()) return
      rootTerms << tree(it)
    }
    return rootTerms;
  }


  def tree (Term term) {
    def t = [:]
    t.name = term.getName()
    t.id = term.getId()
    t.text = term.getName()
    t.data = term.getName()
    t.class = term.class
    t.attr = [ "id" : term.id, "type" : term.class]
    t.checked = false
    t.children = []
    term.relationTerm1.each() { relationTerm->
      if (relationTerm.getRelation().getName() == RelationTerm.names.PARENT) {
        def child = tree(relationTerm.getTerm2())
        t.children << child
      }
    }
    return t
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Ontology.class
    JSON.registerObjectMarshaller(Ontology) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name

      returnArray['attr'] = [ "id" : it.id, "type" : it.class]
      returnArray['data'] = it.name


      returnArray['state'] = "open"

      def terms = []
      if(it.version!=null){
        Term.findAllByOntology(it).each {
          def term = [:]
          term.id = it.getId()
          term.text = it.getName()
          term.class = it.class
          term.attr = [ "id" : it.id, "type" : it.class]
          term.data = it.getName()


          term.checked = false
          term.leaf = false
          terms << term
        }
      }
      returnArray['children'] = it.tree()
      //returnArray['children'] = terms

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
    println "jsonOntology.name=" + jsonOntology.name
    println "ontology.name=" +  ontology.name
    return ontology;
  }

}
