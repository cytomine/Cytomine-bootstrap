package be.cytomine.processing

import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

/**
 * An image filter applies image operations (Binary, Eosin,...)
 */
@RestApiObject(name = "image filter", description = "An image filter applies image operations (Binary, Eosin,...)")
class ImageFilter {

    @RestApiObjectField(description = "The filter name",useForCreation = false)
    String name

    @RestApiObjectField(description = "The URL path of the filter on the processing server",useForCreation = false)
    String baseUrl

    @RestApiObjectField(description = "The URL of the processing server", allowedType = "string",useForCreation = false)
    ProcessingServer processingServer

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "id", description = "The domain id",allowedType = "long",useForCreation = false)
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
