package be.cytomine.social

import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.CytomineDomain

class SharedAnnotation extends CytomineDomain {

    User from

    String comment
    Annotation annotation

    static hasMany = [to : User]

    static constraints = {
    }
    
    String toString() {
        "Annotation shared from " + from.toString() + " to " + to.toString()
    }
}
