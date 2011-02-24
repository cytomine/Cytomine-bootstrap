package be.cytomine.command.user

import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import be.cytomine.security.SecUserSecRole
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class DeleteUserCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    User user = User.findById(postData.id)
    data = user.encodeAsJSON()

    if (!user) {
      return [data : [success : false, message : "User not found with id: " + postData.id], status : 404]
    }

    /*def userGroups = UserGroup.findAllByUser(user)
    if (userGroups != null && userGroups.size() > 0) {
      return [data : [success : false, errors : "User is in one or more groups: " + userGroups.toString()], status : 403]
    }*/

    SecUserSecRole.removeAll(user)  //should we do that ? maybe we should create RemoveSecRole command and make a transaction
    user.delete();
    return [data : [success : true,callback : "Cytomine.Views.User.reload()", message: messageSource.getMessage('be.cytomine.DeleteUserCommand', [postData.username] as Object[], Locale.ENGLISH), data : [user : postData.id]], status : 200]
  }

  def undo() {
    def userData = JSON.parse(data)
    User user = new User(userData)
    user.save()
    return [data : [success : true, user : user,  callback : "Cytomine.Views.User.reload()", message: messageSource.getMessage('be.cytomine.AddUserCommand', [user.username] as Object[], Locale.ENGLISH)], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    User user = User.findById(postData.id)
    user.delete();
    return [data : [success : true,  callback : "Cytomine.Views.User.reload()", message: messageSource.getMessage('be.cytomine.DeleteUserCommand', [postData.username] as Object[], Locale.ENGLISH)], status : 200]

  }
}
