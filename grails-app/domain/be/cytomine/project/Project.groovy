package be.cytomine.project

import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi

class Project extends SequenceDomain {

  String name
  Ontology ontology

  static hasMany = [projectSlide:ProjectSlide, projectGroup:ProjectGroup]

  static constraints = {
    name ( maxSize : 100, unique : true)
  }

  /*static mapping = {
    id generator : "assigned"
  } */

  String toString() {
    name
  }

  static Project createProjectFromData(jsonProject) {
    def project = new Project()
    getProjectFromData(project,jsonProject)
  }

  static Project getProjectFromData(project,jsonProject) {
    String name = jsonProject.name.toString()
    if(!name.equals("null"))
      project.name = jsonProject.name
    else throw new IllegalArgumentException("Project name cannot be null")
    project.ontology = Ontology.get(jsonProject.ontology)
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

      returnArray['image'] = UrlApi.getImageURLWithProjectId(it.id)
      returnArray['term'] = UrlApi.getTermsURLWithOntologyId(it.ontology.id)

      return returnArray
    }
  }
}
