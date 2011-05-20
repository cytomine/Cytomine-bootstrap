package be.cytomine.security

import grails.converters.JSON

class User extends SecUser {

  def springSecurityService

  String firstname
  String lastname
  String email
  String color

  int transaction

  static constraints = {
    firstname blank : false
    lastname blank : false
    email (blank : false , email : true)
  }

  String toString() {
    firstname + " " + lastname + " (" + username + ")"
  }


  static User getUserFromData(User user, jsonUser) {
    println "getUserFromData 1"
    user.username = jsonUser.username
    user.firstname = jsonUser.firstname
    user.lastname = jsonUser.lastname
    println "getUserFromData 2"
    user.email = jsonUser.email
    user.password = user.springSecurityService.encodePassword(jsonUser.password)
    user.enabled = true
    println "getUserFromData 3"
//    user.created = (!jsonUser.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.created)) : null
//    user.updated = (!jsonUser.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.updated)) : null
    println "getUserFromData 4"
    return user;
  }

  static User createUserFromData(data) {
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
	  returnArray['color'] = it.color

      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null

      return returnArray
    }
  }


}
