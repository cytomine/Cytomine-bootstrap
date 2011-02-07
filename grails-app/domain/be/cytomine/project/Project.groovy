package be.cytomine.project

import grails.converters.JSON

class Project {

  String name
  Date created
  /*Date updated
  Date deleted*/

  static hasMany = [projectSlide:ProjectSlide, projectGroup:ProjectGroup]

  static constraints = {
    name ( maxSize : 100)
    /*updated (nullable : true)
    deleted (nullable : true)*/
  }

  String toString() {
    name
  }

  static Project getProjectFromData(data) {
    def project = new Project()
    project.name = data.project.name
    project.created = new Date()
    return project;
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Project.class
    JSON.registerObjectMarshaller(Project) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      return returnArray
    }
  }
}
