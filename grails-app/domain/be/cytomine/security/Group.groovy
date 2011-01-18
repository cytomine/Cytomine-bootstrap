package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup

class Group {

  String name

  static belongsTo = Project
  static hasMany = [userGroup:UserGroup, projectGroup:ProjectGroup]


  static mapping = {
    table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
  }

  String toString() {
    name
  }

}
