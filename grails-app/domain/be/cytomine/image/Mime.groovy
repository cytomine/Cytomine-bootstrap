package be.cytomine.image

import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer

/**
 * Image Extension
 */
class Mime implements Serializable {

    String extension
    String mimeType

    static belongsTo = ImageServer
    static hasMany = [mis: MimeImageServer]

    static constraints = {
        extension(maxSize: 5, blank: false, unique: true)
        mimeType(blank: false, unique: false)
    }

    def imageServers() {
        if (mis != null)
            return mis.collect {it.imageServer}
        else
            return []
    }

    String toString() {
        extension
    }

}
