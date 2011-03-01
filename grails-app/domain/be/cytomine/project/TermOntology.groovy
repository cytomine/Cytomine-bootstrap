package be.cytomine.project

import grails.converters.JSON

class TermOntology {

  Term term
  Ontology ontology

  String color //TODO: must be change by Style style (color, thick,...)


  String toString()
  {
    "[" + this.id + " <" + term + "," + ontology + "> color=" + color +"]"
  }

  static TermOntology link(Term term,Ontology ontology) {
     TermOntology.link(null,term,ontology,"FF0000")
  }

  static TermOntology link(TermOntology termOntology) {
    TermOntology.link(null,termOntology.term,termOntology.ontology,termOntology.color)
  }

  static TermOntology link(Long id,TermOntology termOntology) {
    TermOntology.link(id,termOntology.term,termOntology.ontology,termOntology.color)
  }

  static TermOntology link(Long id,Term term,Ontology ontology,String color) {
    def termOntology = TermOntology.findByTermAndOntology(term, ontology)
    if (!termOntology) {
      termOntology = new TermOntology()
      termOntology.id = id
      termOntology.color = color
      term?.addToTermOntology(termOntology)
      ontology?.addToTermOntology(termOntology)
      println "save termOntology"
      termOntology.save(flush : true)
    } else throw new IllegalArgumentException("Term " + term.id + " and ontology " + ontology.id + " are already mapped")
    return termOntology
  }

  static void unlink(Term term, Ontology ontology) {
    def termOntology = TermOntology.findByTermAndOntology(term, ontology)
    println "unlink termOntology="+termOntology
    if (termOntology) {
      term?.removeFromTermOntology(termOntology)
      ontology?.removeFromTermOntology(termOntology)
      termOntology.delete(flush : true)
    }
  }

  static TermOntology createTermOntologyFromData(jsonTermOntology) {
    def termOntology = new TermOntology()
    getTermOntologyFromData(termOntology,jsonTermOntology)
  }

  static TermOntology getTermOntologyFromData(termOntology,jsonTermOntology) {
    println "jsontermOntology from gettermOntologyFromData = " + jsonTermOntology
    termOntology.ontology = Ontology.get(jsonTermOntology.ontology.id.toString())
    termOntology.term = Term.get(jsonTermOntology.term.id.toString())
    termOntology.color = jsonTermOntology.color
    return termOntology;
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + TermOntology.class
    JSON.registerObjectMarshaller(TermOntology) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['term'] = it.term
      returnArray['ontology'] = it.ontology
      returnArray['color'] = it.color
      return returnArray
    }
  }

}
