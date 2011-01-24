package be.cytomine.security

import grails.converters.JSON

class User extends SecUser {

  def springSecurityService

  String firstname
  String lastname
  String email
  Date dateCreated = new Date()

  static constraints = {
    firstname blank : false
    lastname blank : false
    email (blank : false)
    dateCreated blank : false
  }

  String toString() {
    firstname + lastname + " (" + username + ")"
  }


  static User getUserFromData(data) {
    def userData = JSON.parse(data)
    def user = new User()
    user.username = userData.user.username
    user.firstname = userData.user.firstname
    user.lastname = userData.user.lastname
    user.email = userData.user.email
    user.password = user.springSecurityService.encodePassword(userData.user.password)
    user.enabled = true
    return user;
  }

}
