package be.cytomine.project

class TermOntology {

  Term term
  Ontology ontology

  static mapping = {
    version false
  }

  static TermOntology link(Term term,Ontology ontology) {
    def termOntology = TermOntology.findByTermAndOntology(term, ontology)
    if (!termOntology) {
      termOntology = new TermOntology()
      term?.addToTermOntology(termOntology)
      ontology?.addToTermOntology(termOntology)
      termOntology.save(flush : true)
    }
    return termOntology
  }

  static void unlink(Term term, Ontology ontology) {
    def termOntology = TermOntology.findByTermAndOntology(term, ontology)
    if (termOntology) {
      term?.removeFromTermOntology(termOntology)
      ontology?.removeFromTermOntology(termOntology)
      termOntology.delete(flush : true)
    }

  }
}
