package be.cytomine.processing

import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * An image filter applies image operations (Binary, Eosin,...)
 */
class ImageFilter {

    String name
    String baseUrl
    ProcessingServer processingServer

    static constraints = {
        name(blank: false, nullable: false)
        baseUrl(blank: false, nullable: false)
        processingServer (nullable: true)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageFilter.class)
        JSON.registerObjectMarshaller(ImageFilter) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['processingServer'] = it.processingServer
            returnArray['baseUrl'] = it.baseUrl
            return returnArray
        }
    }
}
