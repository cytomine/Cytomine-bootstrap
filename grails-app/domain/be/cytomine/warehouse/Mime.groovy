package be.cytomine.warehouse

import be.cytomine.server.ImageServer

class Mime {

    String extension
    String mimeType

    static constraints = {
      extension (maxSize : 5, blank : false)
      mimeType blank : false
    }

    static belongsTo = {imageServer : ImageServer}

    String toString() {
      extension
    }
}
