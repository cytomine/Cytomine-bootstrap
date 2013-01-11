package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils
import be.cytomine.security.SecUser

/**
 * An ontology is a list of term
 * Each term may be link to other term with a special relation (parent, synonym,...)
 */
class Ontology extends CytomineDomain implements Serializable {

    String name
    User user

    static constraints = {
        name(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Ontology.withNewSession {
            if(name) {
                Ontology ontology = Ontology.findByName(name)
                if(ontology!=null && (ontology.id!=id))  throw new AlreadyExistException("Ontology " + ontology.name + " already exist!")
            }
        }
    }

    /**
     * Get all ontology terms
     * @return Term list
     */
    def terms() {
        Term.findAllByOntology(this)
    }

    /**
     * Get all term from ontology that have no children (forget 'parent' term)
     * @return
     */
    def leafTerms() {
        Relation parent = Relation.findByName(RelationTerm.names.PARENT)
        if(!parent) {
            return []
        } else {
           return Term.executeQuery('SELECT term FROM Term as term WHERE ontology = :ontology AND term.id NOT IN (SELECT DISTINCT rel.term1.id FROM RelationTerm as rel, Term as t WHERE rel.relation = :relation AND t.ontology = :ontology AND t.id=rel.term1.id)',['ontology':this,'relation':parent])
        }
    }

    /**
     * Get the full ontology (with term) formatted in tree
     * @return List of root parent terms, each root parent term has its own child tree
     */
    def tree() {
        def rootTerms = []
        Relation relation = Relation.findByName(RelationTerm.names.PARENT)
        this.terms().each {
            if (!it.isRoot()) return
            rootTerms << branch(it, relation)
        }
        rootTerms.sort { a, b ->
            if (a.isFolder != b.isFolder) {
                a.isFolder <=> b.isFolder
            } else {
                a.name <=> b.name
            }
        }
        return rootTerms;
    }

    /**
     * Get all projects that use this ontology
     * @return Ontology projects
     */
    def projects() {
        if(this.version){
            Project.findAllByOntology(this)
        } else {
            return []
        }

    }

    /**
     * Get the term branch
     * @param term Root term
     * @param relation Parent relation
     * @return Branch with all term children as tree
     */
    def branch(Term term, Relation relation) {
        def t = [:]
        t.name = term.getName()
        t.id = term.getId()
        t.title = term.getName()
        t.data = term.getName()
        t.color = term.getColor()
        t.class = term.class
        RelationTerm childRelation = RelationTerm.findByRelationAndTerm2(relation, term)
        t.parent = childRelation ? childRelation.term1.id : null

        t.attr = ["id": term.id, "type": term.class]
        t.checked = false
        t.key = term.getId()
        t.children = []
        boolean isFolder = false
        term.relationTerm1.each() { relationTerm ->
            if (relationTerm.getRelation().getName() == RelationTerm.names.PARENT) {
                isFolder = true
                def child = branch(relationTerm.getTerm2(), relation)
                t.children << child
            }
        }
        t.children.sort { a, b ->
            if (a.isFolder != b.isFolder)
                a.isFolder <=> b.isFolder
            else a.name <=> b.name
        }
        t.isFolder = isFolder
        t.hideCheckbox = isFolder
        return t
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Ontology.class)
        JSON.registerObjectMarshaller(Ontology) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['title'] = it.name
            returnArray['attr'] = ["id": it.id, "type": it.class]
            returnArray['data'] = it.name
            returnArray['isFolder'] = true
            returnArray['key'] = it.id
            returnArray['hideCheckbox'] = true
            returnArray['user'] = it.user?.id
            returnArray['state'] = "open"
            returnArray['projects'] = it.projects()
            if (it.version != null) {
                returnArray['children'] = it.tree()
            } else {
                returnArray['children'] = []
            }
            return returnArray
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Ontology createFromDataWithId(def json) {
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
    static Ontology createFromData(def json) {
        def ontology = new Ontology()
        insertDataIntoDomain(ontology, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Ontology insertDataIntoDomain(def domain, def json) {
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        return domain;
    }

}
