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
     * This Method is called during application start
     */
    static void registerMarshaller() {

        Logger.getLogger(this).info("Register custom JSON renderer for " + SharedAnnotation.class)
        JSON.registerObjectMarshaller(SharedAnnotation) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['comment'] = it.comment
            returnArray['sender'] = it.sender.toString()
            returnArray['userannotation'] = it.userAnnotation.id
            returnArray['receivers'] = it.receivers?.collect { it.toString() }
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            return returnArray
        }
    }
}
