package be.cytomine.project

class Ontology {

  String name

  static hasMany = [ termOntology: TermOntology ]

  static constraints = {
    name(blank:false, unique:true)
  }

  def terms() {
    def list = []
    return termOntology.collect{
      it.term.color = it.color
      it.term
    }
  }
}
