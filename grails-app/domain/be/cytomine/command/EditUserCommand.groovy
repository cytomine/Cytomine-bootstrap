package be.cytomine.command

import be.cytomine.security.User
import grails.converters.JSON

class EditUserCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)
    def updatedUser = User.get(postData.user.id)
    def backup = updatedUser.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

    if (!updatedUser ) {
      return [data : [success : false, message : "User not found with id: " + postData.user.id], status : 404]
    }

    //encode the password if necessary
    if (updatedUser.password != postData.user.password)
      postData.user.password = updatedUser.springSecurityService.encodePassword(postData.user.password)

    for (property in postData.user) {

      updatedUser.properties.put(property.key, property.value)
    }


    if ( updatedUser.validate()) {
      data = ([ previousUser : (JSON.parse(backup)), newUser :  updatedUser]) as JSON
      updatedUser.save()
      return [data : [success : true, message:"ok", user :  updatedUser], status : 200]
    } else {
      return [data : [user :  updatedUser, errors : [ updatedUser.errors]], status : 403]
    }


  }

  def undo() {
    def usersData = JSON.parse(data)
    User user = User.findById(usersData.previousUser.id)
    user.username = usersData.previousUser.username
    user.firstname = usersData.previousUser.firstname
    user.lastname = usersData.previousUser.lastname
    user.email = usersData.previousUser.email
    user.password = usersData.previousUser.password
    user.email = usersData.previousUser.email
    user.save()
    return [data : [success : true, message:"ok", user : user], status : 200]
  }

  def redo() {
    def usersData = JSON.parse(data)
    User user = User.findById(usersData.newUser.id)
    user.username = usersData.newUser.username
    user.firstname = usersData.newUser.firstname
    user.lastname = usersData.newUser.lastname
    user.email = usersData.newUser.email
    user.password = usersData.newUser.password
    user.email = usersData.newUser.email
    user.save()
    return [data : [success : true, message:"ok", user : user], status : 200]
  }

}
