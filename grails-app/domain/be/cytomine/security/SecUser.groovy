package be.cytomine.security

import be.cytomine.SequenceDomain
import be.cytomine.ontology.Ontology
import be.cytomine.image.AbstractImage
import be.cytomine.project.Slide

class SecUser extends SequenceDomain {

  String username
  String password
  boolean enabled
  boolean accountExpired
  boolean accountLocked
  boolean passwordExpired
  Boolean transactionInProgress = false //indicates whether the current user is doing several actions seen as only one action

  static transients = ["currentTransaction", "nextTransaction"]

  static hasMany = [userGroup:UserGroup]

  static constraints = {
    username blank: false, unique: true
    password blank: false
    id unique : true
  }

  static mapping = {
    password column: '`password`'
    id (generator:'assigned', unique : true)
  }

  def projects() {
    def projects = []
    userGroup.each { userGroup ->
      userGroup.group.projects().each { project ->
        if(!projects.contains(project))
          projects << project
      }
    }
    projects
  }

  def abstractimages() {
    def abstractimages = []
    userGroup.each { userGroup ->
      def imagesFromGroup =  userGroup.group.abstractimagegroup
      imagesFromGroup.each { abstractImageGroup ->
        AbstractImage image = abstractImageGroup.abstractimage
        if(!abstractimages.contains(image)) {
            abstractimages << image
        }

      }
    }
    abstractimages
  }

  def slides() {
    def slides = []
    userGroup.each { userGroup ->
      def imagesFromGroup =  userGroup.group.abstractimagegroup
      imagesFromGroup.each { abstractImageGroup ->
        AbstractImage image = abstractImageGroup.abstractimage
        Slide slide =  abstractImageGroup.abstractimage.slide
        if(!slides.contains(slide)) {
            slides << slide
        }

      }
    }
    slides
  }

  Set<SecRole> getAuthorities() {
    SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
  }


}
