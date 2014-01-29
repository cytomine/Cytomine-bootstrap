package be.cytomine.ontology

import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import jsondoc.annotation.ApiObjectFieldLight
import org.jsondoc.core.annotation.ApiObject


@ApiObject(name = "annotation index")
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
}
