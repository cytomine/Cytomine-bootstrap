package be.cytomine.server

import be.cytomine.warehouse.Mime

class ImageServer {

  String name
  String url
  String className

  static hasMany = [mimes:Mime]

  static constraints = {
    name blank : false
    url  blank : false
  }

  String toString() {
    name
  }
}
