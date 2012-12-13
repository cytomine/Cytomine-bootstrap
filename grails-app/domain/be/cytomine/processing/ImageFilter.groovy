package be.cytomine.processing

import grails.converters.JSON
import org.apache.log4j.Logger

class ImageFilter {

    String name
    String baseUrl
    ProcessingServer processingServer

    static hasMany = [imageFilterProjects: ImageFilterProject]

    static constraints = {
        name(blank: false, nullable: false)
        baseUrl(blank: false, nullable: false)
        processingServer (nullable: true)
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageFilter.class)
        JSON.registerObjectMarshaller(ImageFilter) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['processingServer'] = it.processingServer.url
            returnArray['baseUrl'] = it.baseUrl
            return returnArray
        }
    }
}
