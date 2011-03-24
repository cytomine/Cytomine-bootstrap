package be.cytomine.project

import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi
import be.cytomine.ontology.Ontology

class Project extends SequenceDomain {

  String name
  Ontology ontology

  static hasMany = [projectSlide:ProjectSlide, projectGroup:ProjectGroup]

  static constraints = {
    name ( maxSize : 100, unique : true)
  }

  String toString() {
    name
  }

  def groups() {
    return projectGroup.collect{
      it.group
    }
  }

  def users() {
    def users = []
    projectGroup.each { projGroup ->
      projGroup.group.users().each { user ->
        if(!users.contains(user))
          users << user
      }
    }
    users
  }

  static Project createProjectFromData(jsonProject) {
    def project = new Project()
    getProjectFromData(project,jsonProject)
  }

  static Project getProjectFromData(project,jsonProject) {
    String name = jsonProject.name.toString()
    /*println "jsonProject.name=" + jsonProject.name
    println "jsonProject.name==null" + (jsonProject.name==null)
    println "jsonProject.name.type" + jsonProject.name.class
    println "jsonProject.name.equals(null)" + jsonProject.name.equals("null")
    println "jsonProject.name.toString().equals(null)" + jsonProject.name.toString().equals("null")
    println "isNull(String key) " + jsonProject.isNull(jsonProject.name)
    println "isNull(String key) " + jsonProject.isNull(name)
    println "isNull(String key) " + jsonProject.isNull("name")  */
    if(!name.equals("null"))
      project.name = jsonProject.name
    else throw new IllegalArgumentException("Project name cannot be null")
    if (jsonProject.ontology)
      project.ontology = Ontology.read(jsonProject.ontology)
    return project;
  }


  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Project.class
    JSON.registerObjectMarshaller(Project) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['ontology'] = it.ontology? it.ontology.id : null
      returnArray['ontologyURL'] = UrlApi.getOntologyURLWithOntologyId(it.ontology?.id)
      returnArray['imageURL'] = UrlApi.getImageURLWithProjectId(it.id)
      returnArray['termURL'] = UrlApi.getTermsURLWithOntologyId(it.ontology?.id)
      returnArray['userURL'] = UrlApi.getUsersURLWithProjectId(it.id)

      return returnArray
    }
  }
}
