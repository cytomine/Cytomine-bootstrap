package be.cytomine.security

import grails.converters.JSON

class User extends SecUser {

  def springSecurityService

  String firstname
  String lastname
  String email

  static constraints = {
    firstname blank : false
    lastname blank : false
    email (blank : false , email : true)
  }

  String toString() {
    firstname + " " + lastname + " (" + username + ")"
  }


  static User getUserFromData(User user, data) {
    user.username = data.username
    user.firstname = data.firstname
    user.lastname = data.lastname
    user.email = data.email
    user.password = user.springSecurityService.encodePassword(data.password)
    user.enabled = true
    return user;
  }

  static User getUserFromData(data) {
    getUserFromData(new User(), data)
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + User.class
    JSON.registerObjectMarshaller(User) {
      def returnArray = [:]
      returnArray['id'] = it.id
      returnArray['username'] = it.username
      returnArray['firstname'] = it.firstname
      returnArray['lastname'] = it.lastname
      returnArray['email'] = it.email
      returnArray['password'] = "******"
      return returnArray
    }
  }


}
