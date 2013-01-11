package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils

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
     * Add a new relation for term1 and term2
     */
    static RelationTerm link(Relation relation, Term term1, Term term2) {
        link(-1, relation, term1, term2)
    }

    /**
     * Add a new relation for term1 and term2
     */
    static RelationTerm link(long id, Relation relation, Term term1, Term term2) {
        if (!relation) {
            throw new WrongArgumentException("Relation cannot be null")
        }
        if (!term1) {
            throw new WrongArgumentException("Term 1 cannot be null")
        }
        if (!term2) {
            throw new WrongArgumentException("Term 2 cannot be null")
        }

        def relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) {
            relationTerm = new RelationTerm()
            if (id != -1) {
                relationTerm.id = id
            }
            term1?.addToRelationTerm1(relationTerm)
            term2?.addToRelationTerm2(relationTerm)
            relation?.addToRelationTerm(relationTerm)
            term1.refresh()
            term2.refresh()
            relation.refresh()
            relationTerm.save(flush: true)
        } else {
            throw new AlreadyExistException("Term1 " + term1.id + " and " + term2.id + " are already mapped with relation " + relation.id)
        }
        return relationTerm
    }

    /**
     * Remove a relation between term1 and term2
     */
    static void unlink(Relation relation, Term term1, Term term2) {
        def relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (relationTerm) {
            term1?.removeFromRelationTerm1(relationTerm)
            term2?.removeFromRelationTerm2(relationTerm)
            relation?.removeFromRelationTerm(relationTerm)
            relationTerm.delete(flush: true)
        }
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
        boolean isNested = false
        try {
            json.relation.id
            isNested = true
        } catch(Exception e) {
            isNested = false
        }


        if(!isNested) {
            domain.relation = JSONUtils.getJSONAttrDomain(json, "relation", new Relation(), true)
            domain.term1 = JSONUtils.getJSONAttrDomain(json, "term1", new Term(), true)
            domain.term2 = JSONUtils.getJSONAttrDomain(json, "term2", new Term(), true)
        } else {
            domain.relation = JSONUtils.getJSONAttrDomain(json.relation, "id", new Relation(), true)
            domain.term1 = JSONUtils.getJSONAttrDomain(json.term1, "id", new Term(), true)
            domain.term2 = JSONUtils.getJSONAttrDomain(json.term2, "id", new Term(), true)
        }
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + RelationTerm.class)
        JSON.registerObjectMarshaller(RelationTerm) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['relation'] = it.relation
            returnArray['term1'] = it.term1
            returnArray['term2'] = it.term2

            return returnArray
        }
    }

}
