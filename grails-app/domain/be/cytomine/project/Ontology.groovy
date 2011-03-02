package be.cytomine.project

class Ontology {

  String name

  static constraints = {
    name(blank:false, unique:true)
  }

  def terms() {
      Term.findAllByOntology(this)
  }
}
