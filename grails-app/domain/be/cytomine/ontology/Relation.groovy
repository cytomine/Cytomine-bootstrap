package be.cytomine.ontology

import be.cytomine.CytomineDomain
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A relation between terms (e.g. term1 PARENT term2)
 */
class Relation extends CytomineDomain implements Serializable {

    String name

    static constraints = {
        name(unique: true, nullable: false)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    String toString() {
        return name
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Relation.class)
        JSON.registerObjectMarshaller(Relation) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

}
