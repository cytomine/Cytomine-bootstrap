package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

class Term extends CytomineDomain implements Serializable, Comparable {

    String name
    String comment
    Ontology ontology
    String color
    Double rate

    static belongsTo = [ontology: Ontology]
    static transients = ["rate"]
    static hasMany = [annotationTerm: AnnotationTerm, relationTerm1: RelationTerm, relationTerm2: RelationTerm]
    //must be done because RelationTerm has two Term attribute
    static mappedBy = [relationTerm1: 'term1', relationTerm2: 'term2']

    static constraints = {
        comment(blank: true, nullable: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    void checkAlreadyExist() {
        Term.withNewSession {
            Term termAlreadyExist=Term.findByNameAndOntology(name, ontology)
            if(termAlreadyExist!=null && (termAlreadyExist.id!=id))  throw new AlreadyExistException("Term " + termAlreadyExist?.name + " already exist!")
        }
    }

    def annotations() {
        def annotations = []
        annotationTerm.each {
            if (!annotations.contains(it.userAnnotation))
                annotations << it.userAnnotation
        }
        annotations
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


    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     * @throws CytomineException Error during domain creation
     */
    static Term createFromDataWithId(def json) throws CytomineException {
        def term = createFromData(json)
        try {term.id = json.id} catch (Exception e) {}
        return term
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Term createFromData(def json) throws CytomineException {
        def term = new Term()
        insertDataIntoDomain(term, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Term insertDataIntoDomain(def domain, def json) throws CytomineException {
        if (!json.name.toString().equals("null"))
            domain.name = json.name
        else throw new WrongArgumentException("Term name cannot be null")
        domain.comment = json.comment

        String ontologyId = json.ontology.toString()
        if (!ontologyId.equals("null")) {
            domain.ontology = Ontology.get(ontologyId)
            if (domain.ontology == null) throw new WrongArgumentException("Ontology was not found with id:" + ontologyId)
        }
        else domain.ontology = null

        domain.color = json.color
        return domain;
    }

    def getCallBack() {
        return [ontologyID: this?.ontology?.id]
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Term.class)
        JSON.registerObjectMarshaller(Term) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['comment'] = it.comment
            returnArray['ontology'] = it.ontology?.id
            try {returnArray['rate'] = it.rate} catch (Exception e) {log.info e}
            RelationTerm rt = RelationTerm.findByRelationAndTerm2(Relation.findByName(RelationTerm.names.PARENT), Term.read(it.id))
            returnArray['parent'] = rt?.term1?.id
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
    
    String toString() {
        name
    }

    int compareTo(Object t) {
        return this.name.compareTo(((Term)t).name)
    }
}
