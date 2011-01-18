package be.cytomine.project

import be.cytomine.security.Group

class Project {

  String name
  Date created
  Date updated
  Date deleted

  static hasMany = [projectSlide:ProjectSlide, projectGroup:ProjectGroup]

  //static belongsTo = Group

  static constraints = {
    name ( maxSize : 100)
    updated (nullable : true)
    deleted (nullable : true)
  }

  String toString() {
    name
  }
}
