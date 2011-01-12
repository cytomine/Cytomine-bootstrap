package be.cytomine.server

class ImageServerParameters {

    String key
    String value

    static belongsTo = [imageServer:ImageServer]

    static constraints = {
      key (blank : false, maxSize : 255)
      value (blank : false, maxSize : 255)
    }
}
