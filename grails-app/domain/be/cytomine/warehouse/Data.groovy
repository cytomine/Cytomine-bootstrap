package be.cytomine.warehouse

class Data {

  String path
  Mime mime

  static constraints = {
    path (maxSize : 255, blank : false)
    mime blank : false
  }

  String toString() {
    path
  }
}
