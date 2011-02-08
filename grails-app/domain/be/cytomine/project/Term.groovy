package be.cytomine.project

class Term {

  String name
  String comment

  static belongsTo = [parent:Term]
  static hasMany = [ child : Term ]

    static constraints = {
    }



}
