package be.cytomine.command

import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import be.cytomine.security.SecUserSecRole

class DeleteUserCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    User user = User.findById(postData.id)
    data = user.encodeAsJSON()

    if (!user) {
      return [data : [success : false, message : "User not found with id: " + postData.id], status : 404]
    }

    def userGroups = UserGroup.findAllByUser(user)
    if (userGroups != null && userGroups.size() > 0) {
      return [data : [success : false, message : "User is in one or more groups: " + userGroups.toString()], status : 403]
    }

    SecUserSecRole.removeAll(user)  //should we do that ? maybe we should create RemoveSecRole command and make a transaction
    user.delete();
    return [data : [success : true, message : "OK"], status : 204]
  }

  def undo() {
    def userData = JSON.parse(data)
    User user = new User(userData)
    user.save()
    return [data : [success : true, user : user, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    User user = User.findById(postData.id)
    user.delete();
    return [data : [success : true, message : "OK"], status : 204]

  }
}
