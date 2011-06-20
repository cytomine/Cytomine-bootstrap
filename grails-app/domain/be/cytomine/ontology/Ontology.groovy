package be.cytomine.ontology
import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.security.User
import be.cytomine.project.Project

class Ontology extends SequenceDomain implements Serializable{

  String name
  User user

  static constraints = {
    name(blank:false, unique:true)
  }
    static mapping = {
    id (generator:'assigned', unique : true)
  }

  def users() {
    def users = []
    def projects = Project.findAllByOntology(this)
    projects.each { project ->
        users.addAll(project.users())
    }
    users
  }
  def usersId() {
    def users = this.users()
    def usersId = []
    users.each{user-> usersId << user.id }
    usersId.unique()
  }

  def terms() {
    Term.findAllByOntology(this)
  }

  def leafTerms() {
    def leafTerms = []
    def terms = Term.findAllByOntology(this)
    terms.each { term ->
       if(!term.hasChildren()) leafTerms << term
    }
    return leafTerms
  }

  def termsParent() {
    Term.findAllByOntology(this)
    //TODO: Check RelationTerm to remove term which have parents
  }

  def tree () {
    def rootTerms = []
    this.terms().each {
      if (!it.isRoot()) return
      rootTerms << branch(it)
    }
    rootTerms.sort { a, b ->
      if(a.isFolder!=b.isFolder)
        a.isFolder <=> b.isFolder
      else a.name <=> b.name
    }
    return rootTerms;
  }


  def branch (Term term) {
    def t = [:]
    t.name = term.getName()
    t.id = term.getId()
    t.title = term.getName()
    t.data = term.getName()
    t.color = term.getColor()
    t.class = term.class
    RelationTerm rt = RelationTerm.findByRelationAndTerm2(Relation.findByName(RelationTerm.names.PARENT),term)
    t.parent = rt? rt.term1.id : null

    t.attr = [ "id" : term.id, "type" : term.class]
    t.checked = false
    t.key = term.getId()
    t.children = []
    boolean isFolder = false
    term.relationTerm1.each() { relationTerm->
      if (relationTerm.getRelation().getName() == RelationTerm.names.PARENT) {
        isFolder = true
        def child = branch(relationTerm.getTerm2())
        t.children << child
      }
    }
    t.children.sort { a, b ->
      if(a.isFolder!=b.isFolder)
        a.isFolder <=> b.isFolder
      else a.name <=> b.name
    }
    t.isFolder = isFolder
    t.hideCheckbox = isFolder
    return t
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Ontology.class
    JSON.registerObjectMarshaller(Ontology) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['title'] = it.name
      returnArray['attr'] = [ "id" : it.id, "type" : it.class]
      returnArray['data'] = it.name
      returnArray['isFolder'] = true
      returnArray['key'] = it.id
      returnArray['hideCheckbox'] = true
      returnArray['user'] = it.user.id
      returnArray['state'] = "open"

      if(it.version!=null){
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

  static Ontology createFromData(jsonOntology) {
    def ontology = new Ontology()
    getFromData(ontology,jsonOntology)
  }

  static Ontology getFromData(ontology,jsonOntology) {
    if(!jsonOntology.name.toString().equals("null"))
      ontology.name = jsonOntology.name
    else throw new IllegalArgumentException("Ontology name cannot be null")
    ontology.user =  User.get(jsonOntology.user);
    println "jsonOntology.name=" + jsonOntology.name
    println "ontology.name=" +  ontology.name
    return ontology;
  }

}
