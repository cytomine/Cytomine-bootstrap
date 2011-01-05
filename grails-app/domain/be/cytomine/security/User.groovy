package be.cytomine.security

class User extends SecUser {

  String firstname
  String lastname
  String email
  Date dateCreated

  static constraints = {
    firstname blank : false
    lastname blank : false
    email blank : false
    dateCreated blank : false
  }


}
