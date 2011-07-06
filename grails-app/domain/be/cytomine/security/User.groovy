package be.cytomine.security

import grails.converters.JSON
import be.cytomine.ontology.Ontology

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
    color (blank : false, nullable : true)
  }

  String toString() {
    firstname + " " + lastname + " (" + username + ")"
  }

   def ontologies() {
    def ontologies = []
    //add ontology created by this user
    if(this.version!=null) ontologies.addAll(Ontology.findAllByUser(this))
    //add ontology from project which can be view by this user
    userGroup.each { userGroup ->
      userGroup.group.projects().each { project ->
        Ontology ontology = project.ontology
        if(!ontologies.contains(ontology))
          ontologies << ontology
      }
    }
    ontologies
  }

  static User getFromData(User user, jsonUser) {
    user.username = jsonUser.username
    user.firstname = jsonUser.firstname
    user.lastname = jsonUser.lastname
    user.email = jsonUser.email
    user.password = user.springSecurityService.encodePassword(jsonUser.password)
    user.enabled = true
//    user.created = (!jsonUser.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.created)) : null
//    user.updated = (!jsonUser.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.updated)) : null
    return user;
  }

  static User createFromData(data) {
    getFromData(new User(), data)
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
