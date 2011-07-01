package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.SequenceDomain
import be.cytomine.image.AbstractImageGroup

class Group extends SequenceDomain {

  String name

  static belongsTo = Project
  static hasMany = [userGroup:UserGroup, projectGroup:ProjectGroup, abstractimagegroup : AbstractImageGroup]


  static mapping = {
    table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
  }

  String toString() {
    name
  }

  def abstractimages() {
    return abstractimagegroup.collect{
      it.abstractimage
    }
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
