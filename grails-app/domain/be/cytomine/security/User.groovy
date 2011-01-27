package be.cytomine.security

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
    firstname + lastname + " (" + username + ")"
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


}
