package be.cytomine.image.server

import be.cytomine.image.AbstractImage

class Storage {

    String name
    String basePath
    String serviceUrl

    static constraints = {
        name (maxSize : 8, unique : true) //ais storage max length
        basePath (nullable: false, blank : false)
        serviceUrl (nullable: false, blank : false)
    }


}
