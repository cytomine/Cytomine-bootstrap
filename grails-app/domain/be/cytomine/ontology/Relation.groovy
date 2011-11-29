package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON

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

    static Relation createFromDataWithId(json)  {
        def domain = createRelationFromData(json)
        try{domain.id = json.id}catch(Exception e){}
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

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Relation.class
        JSON.registerObjectMarshaller(Relation) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

}
