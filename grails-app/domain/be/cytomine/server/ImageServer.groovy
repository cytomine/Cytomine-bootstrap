package be.cytomine.server

import be.cytomine.warehouse.Mime

class ImageServer {

  String name
  String url

  static hasMany = [ parameters:ImageServerParameters,  mime:Mime ]


  static constraints = {
    name blank : false
    url  blank : false
  }

  String toString() {
    name
  }
}
