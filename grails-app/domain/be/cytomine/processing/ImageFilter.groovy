package be.cytomine.processing

import grails.converters.JSON

class ImageFilter {

    String name
    String baseUrl

    static hasMany = [imageFilterProjects: ImageFilterProject]

    static constraints = {
        name(blank: false, nullable: false)
        baseUrl(blank: false, nullable: false)
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + ImageFilter.class
        JSON.registerObjectMarshaller(ImageFilter) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['baseUrl'] = it.baseUrl
            return returnArray
        }
    }
}
