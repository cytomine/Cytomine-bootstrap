package be.cytomine.project

class Ontology {

  String name

  static belongsTo = Term

  static hasMany = [ termOntology: TermOntology ]

    static constraints = {
      name(blank:false, unique:true)
    }
}
