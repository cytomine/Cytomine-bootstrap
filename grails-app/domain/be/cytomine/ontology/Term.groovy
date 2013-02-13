package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

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
        RelationTerm.findAllByTerm1(this).each {
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
        RelationTerm.findAllByTerm2(this).each {
            isRoot &= (it.getRelation().getName() != RelationTerm.names.PARENT)
        }
        return isRoot
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Term insertDataIntoDomain(def json, def domain = new Term()) throws CytomineException {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
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
     */
    static void registerMarshaller() {
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
