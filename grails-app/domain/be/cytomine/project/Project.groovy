package be.cytomine.project

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import be.cytomine.SequenceDomain

class Project extends SequenceDomain {

  String name

  static hasMany = [projectSlide:ProjectSlide, projectGroup:ProjectGroup]

  static constraints = {
    name ( maxSize : 100)
  }

  String toString() {
    name
  }

  static Project createProjectFromData(jsonProject) {
    def project = new Project()
    getProjectFromData(project,jsonProject)
  }

  static Project getProjectFromData(project,jsonProject) {
    if(!jsonProject.name.toString().equals("null"))
      project.name = jsonProject.name
    else throw new IllegalArgumentException("Project name cannot be null")
    return project;
  }

  def getImageURL() {
    return ConfigurationHolder.config.grails.serverURL + '/api/project/'+ this.id +'/image.json';
  }


  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Project.class
    JSON.registerObjectMarshaller(Project) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['image'] = it.getImageURL()
      return returnArray
    }
  }
}
