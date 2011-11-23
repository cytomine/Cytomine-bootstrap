package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SequenceDomain
import grails.converters.JSON

class Term extends SequenceDomain implements Serializable {

    String name
    String comment

    Ontology ontology
    String color

    Double rate

    static belongsTo = [ontology: Ontology]
    static transients = ["rate"]
    //static belongsTo = Annotation
    static hasMany = [annotationTerm: AnnotationTerm, relationTerm1: RelationTerm, relationTerm2: RelationTerm]

    //must be done because RelationTerm has two Term attribute
    static mappedBy = [relationTerm1: 'term1', relationTerm2: 'term2']

    static constraints = {
        comment(blank: true, nullable: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    def annotations() {
        def annotations = []
        annotationTerm.each {
            if (!annotations.contains(it.annotation))
                annotations << it.annotation
        }
        annotations
    }

    def relationAsTerm1() {
        def relations = []
        relationTerm1.each {
            def map = [:]
            map.put(it.relation, it.term2)
            relations.add(map)
        }
        return relations
    }

    def hasChildren() {
        boolean hasChildren = false
        this.relationTerm1.each {
            if (it.getRelation().getName().equals(RelationTerm.names.PARENT)) {
                hasChildren = true
                return
            }
        }
        return hasChildren
    }

    def isRoot() {
        def isRoot = true;
        this.relationTerm2.each {
            isRoot &= (it.getRelation().getName() != RelationTerm.names.PARENT)
        }
        return isRoot
    }

    def isChild() {
        def isChild = false;
        this.relationTerm2.each {
            isChild |= (it.getRelation().getName() == RelationTerm.names.PARENT)
        }
        return isChild
    }

    def relationAsTerm2() {
        def relations = []
        relationTerm2.each {
            def map = [:]
            map.put(it.relation, it.term1)
            relations.add(map)
        }
        return relations
    }

    static Term createFromDataWithId(jsonTerm) throws CytomineException {
        def term = createFromData(jsonTerm)
        term.id = jsonTerm.id
        return term
    }

    static Term createFromData(jsonTerm) throws CytomineException {
        def term = new Term()
        getFromData(term, jsonTerm)
    }

    static Term getFromData(term, jsonTerm) throws CytomineException {
        if (!jsonTerm.name.toString().equals("null"))
            term.name = jsonTerm.name
        else throw new WrongArgumentException("Term name cannot be null")
        term.comment = jsonTerm.comment

        String ontologyId = jsonTerm.ontology.toString()
        if (!ontologyId.equals("null")) {
            term.ontology = Ontology.get(ontologyId)
            if (term.ontology == null) throw new WrongArgumentException("Ontology was not found with id:" + ontologyId)
        }
        else term.ontology = null

        term.color = jsonTerm.color
        return term;
    }

    def getIdOntology() {
        if (this.ontologyId) return this.ontologyId
        else return this.ontology?.id
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Term.class
        JSON.registerObjectMarshaller(Term) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['comment'] = it.comment
            returnArray['ontology'] = it.getIdOntology()
            try {returnArray['rate'] = it.rate} catch (Exception e) {println e}
            RelationTerm rt = RelationTerm.findByRelationAndTerm2(Relation.findByName(RelationTerm.names.PARENT), Term.read(it.id))

            returnArray['parent'] = rt?.getIdTerm1()
            if (it.color) returnArray['color'] = it.color
            return returnArray
        }
    }

    public boolean equals(Object o) {
        if (!o) return false
        if (!o instanceof Term) return false
        try {return ((Term) o).getId() == this.getId()} catch (Exception e) { return false}
        //if no try/catch, when getting term from ontology => GroovyCastException: Cannot cast object 'null' with class 'org.codehaus.groovy.grails.web.json.JSONObject$Null' to class 'be.cytomine.ontology.Term'

    }
}
