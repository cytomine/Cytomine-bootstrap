package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Relation between a term 1 and a term 2
 */
class RelationTerm extends CytomineDomain implements Serializable {

    static names = [PARENT: "parent", SYNONYM: "synonyme"]

    Relation relation
    Term term1
    Term term2

    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    String toString() {
        "[" + this.id + " <" + relation + '(' + relation?.name + ')' + ":[" + term1 + '(' + term1?.name + ')' + "," + term2 + '(' + term2?.name + ')' + "]>]"
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain on
     */
    static RelationTerm createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static RelationTerm createFromData(def json) {
        def relationTerm = new RelationTerm()
        insertDataIntoDomain(relationTerm, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static RelationTerm insertDataIntoDomain(def domain, def json) {
        domain.relation = JSONUtils.getJSONAttrDomain(json, "relation", new Relation(), true)
        domain.term1 = JSONUtils.getJSONAttrDomain(json, "term1", new Term(), true)
        domain.term2 = JSONUtils.getJSONAttrDomain(json, "term2", new Term(), true)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + RelationTerm.class)
        JSON.registerObjectMarshaller(RelationTerm) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['relation'] = it.relation.id
            returnArray['term1'] = it.term1.id
            returnArray['term2'] = it.term2.id

            return returnArray
        }
    }

    /**
     * Return domain ontology (term ontology, relation-term ontology...)
     * By default, a domain has no ontology linked.
     * You need to override ontologyDomain() in domain class
     * @return Domain ontology
     */
    public Ontology ontologyDomain() {
        return term1.ontology
    }

    void checkAlreadyExist() {
        RelationTerm.withNewSession {
            if(relation && term1 && term2) {
                RelationTerm rt = RelationTerm.findByRelationAndTerm1AndTerm2(relation,term1,term2)
                if(rt!=null && (rt.id!=id))  {
                    throw new AlreadyExistException("RelationTerm with relation=${relation.id} and term1 ${term1.id} and term2 ${term2.id} already exist!")
                }
            }
        }
    }

}
