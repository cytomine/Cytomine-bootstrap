package be.cytomine.social

import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.CytomineDomain
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class SharedAnnotation extends CytomineDomain {

    User sender
    String comment
    Annotation annotation

    static hasMany = [receiver : User]

    static constraints = {
        comment(type: 'text', maxSize: ConfigurationHolder.config.cytomine.maxRequestSize, nullable: true)
    }
    
    String toString() {
        "Annotation shared from " + from.toString() + " to " + to.toString()
    }

    static void registerMarshaller() {

        println "Register custom JSON renderer for " + SharedAnnotation.class
        JSON.registerObjectMarshaller(SharedAnnotation) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['comment'] = it.comment
            returnArray['sender'] = it.sender.toString()
            returnArray['receiver'] = it.receiver.collect { it.toString() }
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            return returnArray
        }
    }
}
