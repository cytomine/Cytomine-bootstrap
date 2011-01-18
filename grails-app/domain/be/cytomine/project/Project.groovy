package be.cytomine.project

import be.cytomine.security.Group

class Project {

  String name
  Date created
  Date updated
  Date deleted

  static hasMany = [scan:Scan, projectScan:ProjectScan, projectGroup:ProjectGroup]

  static belongsTo = Group

  static constraints = {
    name ( maxSize : 100)
    updated (nullable : true)
    deleted (nullable : true)
  }
}
