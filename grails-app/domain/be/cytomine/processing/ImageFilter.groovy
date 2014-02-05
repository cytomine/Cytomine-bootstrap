package be.cytomine.processing

import be.cytomine.CytomineDomain
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * An image filter applies image operations (Binary, Eosin,...)
 */
@ApiObject(name = "image filter", description = "An image filter applies image operations (Binary, Eosin,...)")
class ImageFilter {

    @ApiObjectFieldLight(description = "The filter name",useForCreation = false)
    String name

    @ApiObjectFieldLight(description = "The URL path of the filter on the processing server",useForCreation = false)
    String baseUrl

    @ApiObjectFieldLight(description = "The URL of the processing server", allowedType = "string",useForCreation = false)
    ProcessingServer processingServer

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "id", description = "The domain id",allowedType = "long",useForCreation = false)
    ])
    static constraints = {
        name(blank: false, nullable: false)
        baseUrl(blank: false, nullable: false)
        processingServer (nullable: true)
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = [:]
        returnArray['id'] = domain?.id
        returnArray['name'] = domain?.name
        returnArray['processingServer'] = domain?.processingServer?.url
        returnArray['baseUrl'] = domain?.baseUrl
        return returnArray
    }
}
