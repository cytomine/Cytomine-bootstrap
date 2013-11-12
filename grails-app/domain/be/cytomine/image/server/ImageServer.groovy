package be.cytomine.image.server

import be.cytomine.CytomineDomain

/**
 * Server that provide images data
 */
class ImageServer extends CytomineDomain {

    String name
    String url
    String service
    String className
    Boolean available

    static constraints = {
        name blank: false
        url blank: false
        available nullable: false
    }

    def getBaseUrl() {
        return url + service
    }
}
