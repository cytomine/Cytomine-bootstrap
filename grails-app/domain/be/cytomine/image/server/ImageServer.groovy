package be.cytomine.image.server

import be.cytomine.image.Mime
import be.cytomine.SequenceDomain

class ImageServer extends SequenceDomain {

    String name
    String url
    String service
    String className
    Storage storage
    Boolean available

    static hasMany = [mimes:Mime, mis:MimeImageServer]

    static constraints = {
        name blank : false
        url  blank : false
        mimes nullable : true
        storage nullable : true
        available nullable : false
    }

    String toString() {
        name
    }

    def mimes() {
        return mis.collect{it.Mime}
    }

    def getBaseUrl() {
        return url + service
    }

    def getZoomifyUrl() {
        return url + service + "?zoomify=" + storage.getBasePath()
    }
}
