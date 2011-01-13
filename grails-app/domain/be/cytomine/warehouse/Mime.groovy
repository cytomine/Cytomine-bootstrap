package be.cytomine.warehouse

import be.cytomine.server.ImageServer

class Mime {

    String extension
    String mimeType

    static belongsTo = [imageServer:ImageServer]

    static constraints = {
      extension (maxSize : 5, blank : false)
      mimeType (blank : false, unique : true)
    }

    String toString() {
      extension
    }
}
