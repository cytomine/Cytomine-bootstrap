package be.cytomine.security

class User extends SecUser {

  String firstname
  String lastname
  String email
  Date dateCreated
  String justForATestYouCanDeleteMe

  static constraints = {
    firstname blank : false
    lastname blank : false
    email (blank : false, email:true)
    dateCreated blank : false
    justForATestYouCanDeleteMe nullable : true
  }


}
