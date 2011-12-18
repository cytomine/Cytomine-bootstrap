package be.cytomine.social

import be.cytomine.ontology.Annotation
import be.cytomine.security.User

class SharedAnnotation {

    User from
    User to
    String comment
    Annotation annotation

    static constraints = {
    }
    
    String toString() {
        "Annotation shared from " + from.toString() + " to " + to.toString()
    }
}
