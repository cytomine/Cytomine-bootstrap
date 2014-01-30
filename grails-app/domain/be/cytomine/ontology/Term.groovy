package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObjectField

/**
 * A term is a class that can be link to an annotation
 * A term is a part of ontology (list/tree of terms)
 */
//@ApiObject(name = "term", description = "Term description", show = true)
class Term extends CytomineDomain implements Serializable, Comparable {

    @ApiObjectFieldLight(description = "The term name")
    String name

    @ApiObjectFieldLight(description = "A comment about the term", mandatory = false)
    String comment

    @ApiObjectFieldLight(description = "The ontology that store the term")
    Ontology ontology

    @ApiObjectFieldLight(description = "The color associated, in HTML format (e.g : RED = #FF0000)")
    String color

    Double rate // ?

    static belongsTo = [ontology: Ontology]

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "parent", description = "The parent term id of this annotation",allowedType = "long",useForCreation = false)
    ])
    static transients = ["rate"]

    static constraints = {
        comment(blank: true, nullable: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
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
        Logger.getLogger(this).info("Register custom JSON renderer for " + this.class)
        JSON.registerObjectMarshaller(Term) { domain ->
            return getDataFromDomain(domain)
        }
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = AnnotationDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['comment'] = domain?.comment
        returnArray['ontology'] = domain?.ontology?.id
        try {returnArray['rate'] = domain?.rate} catch (Exception e) {log.info e}
        try {
            RelationTerm rt = RelationTerm.findByRelationAndTerm2(Relation.findByName(RelationTerm.names.PARENT), Term.read(domain?.id))
            returnArray['parent'] = rt?.term1?.id
        } catch (Exception e) {log.info e}

        if (domain?.color) returnArray['color'] = domain?.color
        return returnArray
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
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return ontology.container();
    }
}
