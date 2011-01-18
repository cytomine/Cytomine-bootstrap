package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup

class Group {

  static mapping = {
    table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
  }

  String name

  static belongsTo = User
  static hasMany = [userGroup:UserGroup, project:Project, projectGroup:ProjectGroup]

}
