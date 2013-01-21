package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils

/**
 * A term is a class that can be link to an annotation
 * A term is a part of ontology (list/tree of terms)
 */
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

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Term.withNewSession {
            Term termAlreadyExist=Term.findByNameAndOntology(name, ontology)
            if(termAlreadyExist!=null && (termAlreadyExist.id!=id))  {
                throw new AlreadyExistException("Term " + termAlreadyExist?.name + " already exist!")
            }
        }
    }

    /**
     * Check if this term has children
     * @return True if this term has children, otherwise false
     */
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

    /**
     * Check if this term has no parent
     * @return True if term has no parent
     */
    def isRoot() {
        def isRoot = true;
        this.relationTerm2.each {
            isRoot &= (it.getRelation().getName() != RelationTerm.names.PARENT)
        }
        return isRoot
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
        domain.name = JSONUtils.getJSONAttrStr(json,'name')
        domain.created = JSONUtils.getJSONAttrDate(json,'created')
        domain.updated = JSONUtils.getJSONAttrDate(json,'updated')
        domain.comment = JSONUtils.getJSONAttrStr(json,'comment')
        domain.color = JSONUtils.getJSONAttrStr(json,'color')
        domain.ontology = JSONUtils.getJSONAttrDomain(json, "ontology", new Ontology(), true)

        if (!domain.name) {
            throw new WrongArgumentException("Term name cannot be null")
        }
        return domain;
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
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
        if (!o) {
            return false
        } else if (!o instanceof Term) {
            return false
        } else {
            try {return ((Term) o).getId() == this.getId()} catch (Exception e) { return false}
        }

    }
    
    String toString() {
        name
    }

    int compareTo(Object t) {
        return this.name.compareTo(((Term)t).name)
    }

    /**
     * Return domain ontology (term ontology, relation-term ontology...)
     * By default, a domain has no ontology linked.
     * You need to override ontologyDomain() in domain class
     * @return Domain ontology
     */
    public Ontology ontologyDomain() {
        return ontology;
    }
}
