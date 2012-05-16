package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON

class Ontology extends CytomineDomain implements Serializable {

    String name
    User user

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
        def leafTerms = []
        def terms = Term.findAllByOntology(this)
        terms.each { term ->
            if (!term.hasChildren()) leafTerms << term
        }
        return leafTerms
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

    def getIdUser() {
//        if (this.userId) return this.userId
//        else return this.user?.id
        return this.user?.id
    }

    def projectService
    def getProjects() {
        def projects = []
        /*projectService.list(this).each { project->
            projects << [ id : project.id, name : project.name]
        }*/
        return projects

    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Ontology.class
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
            returnArray['user'] = it.getIdUser()
            returnArray['state'] = "open"
            returnArray['projects'] = it.getProjects()
            if (it.version != null) {
                returnArray['children'] = it.tree()
                returnArray['users'] = it.usersId()
            }
            else {
                returnArray['children'] = []
                returnArray['users'] = []
            }
            return returnArray
        }
    }

    static Ontology createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Ontology createFromData(jsonOntology) {
        def ontology = new Ontology()
        getFromData(ontology, jsonOntology)
    }

    static Ontology getFromData(ontology, jsonOntology) {
        if (!jsonOntology.name.toString().equals("null"))
            ontology.name = jsonOntology.name
        else throw new WrongArgumentException("Ontology name cannot be null")
        ontology.user = User.get(jsonOntology.user);
        println "jsonOntology.name=" + jsonOntology.name
        println "ontology.name=" + ontology.name
        return ontology;
    }

}
