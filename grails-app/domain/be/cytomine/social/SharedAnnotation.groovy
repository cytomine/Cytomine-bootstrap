package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class SharedAnnotation extends CytomineDomain {

    User sender
    String comment
    UserAnnotation userAnnotation

    static hasMany = [receiver : User]

    static constraints = {
        comment(type: 'text', maxSize: ConfigurationHolder.config.cytomine.maxRequestSize, nullable: true)
    }
    
    String toString() {
        "Annotation " + userAnnotation + " shared by " + sender
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {

        Logger.getLogger(this).info("Register custom JSON renderer for " + SharedAnnotation.class)
        JSON.registerObjectMarshaller(SharedAnnotation) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['comment'] = it.comment
            returnArray['sender'] = it.sender.toString()
            returnArray['userannotation'] = it.userAnnotation.id
            returnArray['receiver'] = it.receiver.collect { it.toString() }
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            return returnArray
        }
    }
}
