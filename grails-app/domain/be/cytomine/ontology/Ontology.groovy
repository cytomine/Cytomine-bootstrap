package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger

class Ontology extends CytomineDomain implements Serializable {

    String name
    User user

    def relationService

    static constraints = {
        name(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    void checkAlreadyExist() {
        Ontology.withNewSession {
            Ontology ontology = Ontology.findByName(name)
            if(ontology!=null && (ontology.id!=id))  throw new AlreadyExistException("Ontology " + ontology.name + " already exist!")
        }
    }

    //TODO:: remove (move in userService)
    def users() {
        def users = []
        def projects = Project.findAllByOntology(this)
        projects.each { project ->
            users.addAll(project.users())
        }
        users.unique()
    }

    def usersId() {
        users().collect {it.id}
    }

    def terms() {
        Term.findAllByOntology(this)
    }

    def leafTerms() {
        //get all term from ontology which are not in term with child
        Relation parent = Relation.findByName(RelationTerm.names.PARENT)
        if(!parent) return []
        def terms = Term.executeQuery('SELECT term FROM Term as term WHERE ontology = :ontology AND term.id NOT IN (SELECT DISTINCT rel.term1.id FROM RelationTerm as rel, Term as t WHERE rel.relation = :relation AND t.ontology = :ontology AND t.id=rel.term1.id)',['ontology':this,'relation':parent])
        return terms
    }

    def termsParent() {
        Term.findAllByOntology(this)
        //TODO: Check RelationTerm to remove term which have parents
    }

    def tree() {
        def rootTerms = []
        Relation relation = Relation.findByName(RelationTerm.names.PARENT)
        this.terms().each {
            if (!it.isRoot()) return
            rootTerms << branch(it, relation)
        }
        rootTerms.sort { a, b ->
            if (a.isFolder != b.isFolder)
                a.isFolder <=> b.isFolder
            else a.name <=> b.name
        }
        return rootTerms;
    }


    def branch(Term term, Relation relation) {
        def t = [:]
        t.name = term.getName()
        t.id = term.getId()
        t.title = term.getName()
        t.data = term.getName()
        t.color = term.getColor()
        t.class = term.class
        RelationTerm rt = RelationTerm.findByRelationAndTerm2(relation, term)
        t.parent = rt ? rt.term1.id : null

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

    def projectService
    def getProjects() {
        def projects = []
        /*projectService.list(this).each { project->
            projects << [ id : project.id, name : project.name]
        }*/
        return projects

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
            returnArray['projects'] = it.getProjects()
            if (it.version != null) returnArray['children'] = it.tree()
            else returnArray['children'] = []
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
        if (!json.name.toString().equals("null")) {
            domain.name = json.name
        } else {
            throw new WrongArgumentException("Ontology name cannot be null")
        }
        domain.user = User.get(json.user);
        return domain;
    }

}
