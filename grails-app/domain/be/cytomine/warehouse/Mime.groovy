package be.cytomine.warehouse

import be.cytomine.server.ImageServer
import be.cytomine.server.MimeImageServer

class Mime {

  String extension
  String mimeType

  static belongsTo = ImageServer
  static hasMany = [mis:MimeImageServer]

  static constraints = {
    extension (maxSize : 5, blank : false)
    mimeType (blank : false, unique : true)
  }

  def imageServers() {
    return mis.collect{it.imageServer}
  }

  String toString() {
    extension
  }

}
