package be.cytomine.image.server

import be.cytomine.image.AbstractImage

/**
 * Property (key-value) map with an image
 */
class ImageProperty {

    String key
    String value
    AbstractImage image

    static constraints = {
        key(nullable: false, empty: false)
        value(nullable: false, empty: false)
        image(nullable: false)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def prop) {
        def returnArray = [:]
        returnArray["id"] = prop?.id
        returnArray["key"] = prop?.key
        returnArray["value"] = prop?.value
        returnArray
    }
}
