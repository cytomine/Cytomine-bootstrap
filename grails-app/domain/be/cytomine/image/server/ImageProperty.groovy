package be.cytomine.image.server

import be.cytomine.image.AbstractImage
import grails.converters.JSON
import org.apache.log4j.Logger

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
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AbstractImage.class)
        JSON.registerObjectMarshaller(ImageProperty) {
            def returnArray = [:]
            returnArray["id"] = it.id
            returnArray["key"] = it.key
            returnArray["value"] = it.value
            return returnArray
        }
    }
}
