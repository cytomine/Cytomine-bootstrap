package be.cytomine.image.server

import be.cytomine.CytomineDomain
import be.cytomine.image.Mime

/**
 * Server that provide images data
 */
class ImageServer extends CytomineDomain {

    String name
    String url
    String service
    String className
    Storage storage //deprecated
    Boolean available

    static constraints = {
        name blank: false
        url blank: false
        storage nullable: true
        available nullable: false
    }

    String toString() {
        name
    }

    def getBaseUrl() {
        return url + service
    }

    def getZoomifyUrl() {
        return url + service + "?zoomify=" + storage.getBasePath()
    }
}
