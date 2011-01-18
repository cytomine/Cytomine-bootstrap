package be.cytomine.security

class User extends SecUser {

  String firstname
  String lastname
  String email
  Date dateCreated

  static belongsTo = Group
  static hasMany = [userGroup:UserGroup]

  static constraints = {
    firstname blank : false
    lastname blank : false
    email (blank : false, email:true)
    dateCreated blank : false
  }



}
