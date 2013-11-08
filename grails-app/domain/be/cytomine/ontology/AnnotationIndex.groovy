package be.cytomine.ontology

import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser

/**
 * An annotation created by a user
 */
class AnnotationIndex implements Serializable {

    SecUser user
    ImageInstance image
    Long countAnnotation
    Long countReviewedAnnotation

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        sort "id"
        cache false
    }
}
