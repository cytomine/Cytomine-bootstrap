package be.cytomine.ontology

import grails.converters.JSON
import be.cytomine.SequenceDomain

class Relation extends SequenceDomain implements Serializable{

    String name

    static hasMany = [relationTerm:RelationTerm]

    static constraints = {
        name (unique:true, nullable:false)
    }
    static mapping = {
        id (generator:'assigned', unique : true)
    }
    String toString()
    {
        return name
    }

    static Relation createRelationFromData(jsonRelation) {
        def relation = new Relation()
        getRelationFromData(relation,jsonRelation)
    }

    static Relation getRelationFromData(relation,jsonRelation) {
        if(!jsonRelation.name.toString().equals("null"))
            relation.name = jsonRelation.name
        else throw new IllegalArgumentException("Relation name cannot be null")
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
