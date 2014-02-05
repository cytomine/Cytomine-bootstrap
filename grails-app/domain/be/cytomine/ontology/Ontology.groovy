package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField

/**
 * An ontology is a list of term
 * Each term may be link to other term with a special relation (parent, synonym,...)
 */
@ApiObject(name = "ontology", description = "An ontology is a list of term. Each term may be link to other term with a special relation (parent, synonym,...)")
class Ontology extends CytomineDomain implements Serializable {

    @ApiObjectFieldLight(description = "The name of the ontology")
    String name

    @ApiObjectFieldLight(description = "The author of the ontology")
    User user

    //TODO: if perf issue, may be save ontology json in a text field. Load json instead ontology marshaller and update json when ontology is updated

//    returnArray['attr'] = ["id": domain?.id, "type": domain?.class]
//    returnArray['data'] = domain?.name
//    returnArray['isFolder'] = true
//    returnArray['key'] = domain?.id
//    returnArray['hideCheckbox'] = true
//    returnArray['state'] = "open"
//    returnArray['projects'] = domain?.projects()
//    if (domain?.version != null) {
//        returnArray['children'] = domain?.tree()
//    } else {
//        returnArray['children'] = []
//    }

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "children", description = "Term Tree",allowedType = "tree",useForCreation = false)
    ])
    static transients = []

    static constraints = {
        name(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Ontology.withNewSession {
            if(name) {
                Ontology ontology = Ontology.findByName(name)
                if(ontology!=null && (ontology.id!=id))  {
                    throw new AlreadyExistException("Ontology " + ontology.name + " already exist!")
                }
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
        if(this.version!=null){
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
        RelationTerm.findAllByTerm1(term).each() { relationTerm ->
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {

        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['user'] = domain?.user?.id

        returnArray['title'] = domain?.name
        returnArray['attr'] = ["id": domain?.id, "type": domain?.class]
        returnArray['data'] = domain?.name
        returnArray['isFolder'] = true
        returnArray['key'] = domain?.id
        returnArray['hideCheckbox'] = true
        returnArray['state'] = "open"
        returnArray['projects'] = domain?.projects()
        if (domain?.version != null) {
            returnArray['children'] = domain?.tree()
        } else {
            returnArray['children'] = []
        }

        return returnArray
    }

    /* Marshaller Helper fro user field */
    private static Integer userID(Ontology ontology) {
        return ontology.getUser()?.id
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Ontology insertDataIntoDomain(def json,def domain = new Ontology()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        return domain;
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }

}
