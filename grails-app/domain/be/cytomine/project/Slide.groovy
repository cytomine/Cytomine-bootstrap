package be.cytomine.project

class Slide {

  String name
  int order

  static mapping = {
    columns {
      order column:"`order`"  //otherwise there is a conflict with the word "ORDER" from the SQL SYNTAX
    }
  }

  String toString() {
    name
  }

  static hasMany = [projectSlide:ProjectSlide, scan:Scan]

  static constraints = {
  }
}
