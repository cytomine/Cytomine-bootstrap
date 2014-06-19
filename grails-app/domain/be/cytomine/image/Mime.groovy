package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.image.server.MimeImageServer

/**
 * Image Extension
 */
class Mime extends CytomineDomain implements Serializable {

    String extension
    String mimeType

    static constraints = {
        extension(maxSize: 5, blank: false, unique: false)
        mimeType(blank: false, unique: true)
    }

    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Get list of image server that support this mime
     * @return Image server list
     */
    def imageServers() {
        MimeImageServer.findAllByMime(this).collect {it.imageServer}
    }

    String toString() {
        extension
    }

}
