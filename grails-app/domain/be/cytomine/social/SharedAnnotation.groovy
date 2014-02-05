package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A shared annotation is a comment on a specific annotation
 * (e.g. is it the good term?, ...)
 * Receiver user can see the comment and add answer
 */
class SharedAnnotation extends CytomineDomain {

    /**
     * User that ask the question
     */
    User sender

    /**
     * Comment that will be share with other user
     */
    String comment

    /**
     * Only user annotation for now (not reviewed/algo annotation)
     */
    UserAnnotation userAnnotation

    static hasMany = [receivers : User]

    static constraints = {
        comment(type: 'text', nullable: true)
    }
    
    String toString() {
        "Annotation " + userAnnotation + " shared by " + sender
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['comment'] = domain?.comment
        returnArray['sender'] = domain?.sender?.toString()
        returnArray['userannotation'] = domain?.userAnnotation?.id
        returnArray['receivers'] = domain?.receivers?.collect { it.toString() }
        returnArray
    }
}
