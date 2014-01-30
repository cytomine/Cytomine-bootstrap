package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject


@ApiObject(name = "annotation index", description="A index entry that store, for an image and a user, the number of annotation created/reviewed")
class AnnotationIndex implements Serializable {

    @ApiObjectFieldLight(description = "The user criteria", useForCreation = false)
    SecUser user

    @ApiObjectFieldLight(description = "The image criteria",useForCreation = false)
    ImageInstance image

    @ApiObjectFieldLight(description = "The number of annotation added by the user (auto incr with trigger)",useForCreation = false)
    Long countAnnotation

    @ApiObjectFieldLight(description = "The number of review added by the user (auto incr with trigger)",useForCreation = false)
    Long countReviewedAnnotation

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        sort "id"
        cache false
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + this.class)
        JSON.registerObjectMarshaller(AnnotationIndex) { domain ->
            return getDataFromDomain(domain)
        }
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['user'] = domain?.user?.id
        returnArray['image'] = domain?.image?.id
        returnArray['countAnnotation'] = domain?.countAnnotation
        returnArray['countReviewedAnnotation'] = domain?.countReviewedAnnotation
        return returnArray
    }
}
