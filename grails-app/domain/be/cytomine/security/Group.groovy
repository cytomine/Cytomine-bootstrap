package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.SequenceDomain

class Group extends SequenceDomain {

  String name

  static belongsTo = Project
  static hasMany = [userGroup:UserGroup, projectGroup:ProjectGroup]


  static mapping = {
    table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
  }

  String toString() {
    name
  }

  def users() {
    return userGroup.collect{
      it.user
    }
  }

  def projects() {
    return projectGroup.collect{
      it.project
    }
  }

}
