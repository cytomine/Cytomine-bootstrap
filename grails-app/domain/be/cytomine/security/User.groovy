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
    email (blank : false , email : true)
    dateCreated blank : false
  }

  String toString() {
    firstname + " " + lastname + " (" + username + ")"
  }


  static User getUserFromData(data) {
    def user = new User()
    user.username = data.user.username
    user.firstname = data.user.firstname
    user.lastname = data.user.lastname
    user.email = data.user.email
    user.password = user.springSecurityService.encodePassword(data.user.password)
    user.enabled = true
    return user;
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
