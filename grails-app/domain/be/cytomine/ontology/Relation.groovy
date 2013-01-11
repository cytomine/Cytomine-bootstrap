package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

class Relation extends CytomineDomain implements Serializable {

    String name

    static hasMany = [relationTerm: RelationTerm]

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
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Relation createFromDataWithId(def json) {
        def domain = createRelationFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Relation createRelationFromData(jsonRelation) {
        def relation = new Relation()
        getRelationFromData(relation, jsonRelation)
    }

    static Relation getRelationFromData(relation, jsonRelation) {
        if (!jsonRelation.name.toString().equals("null"))
            relation.name = jsonRelation.name
        else throw new WrongArgumentException("Relation name cannot be null")
        return relation;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
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
