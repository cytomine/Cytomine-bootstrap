package be.cytomine.image.server

import be.cytomine.image.AbstractImage
import grails.converters.JSON

class ImageProperty {

    String key
    String value
    AbstractImage image

    static constraints = {
        key(nullable: false, empty: false)
        value(nullable: false, empty: false)
        image(nullable: false)
    }

    static void registerMarshaller() {

        println "Register custom JSON renderer for " + AbstractImage.class
        JSON.registerObjectMarshaller(ImageProperty) {
            def returnArray = [:]
            returnArray["id"] = it.id
            returnArray["key"] = it.key
            returnArray["value"] = it.value

            return returnArray
        }
    }
}
