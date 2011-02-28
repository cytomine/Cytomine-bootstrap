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
    def username = user.username
    data = user.encodeAsJSON()

    if (!user) {
      return [data : [success : false, errors : "User not found with id: " + postData.id], status : 404]
    }

    /*def userGroups = UserGroup.findAllByUser(user)
    if (userGroups != null && userGroups.size() > 0) {
      return [data : [success : false, errors : "User is in one or more groups: " + userGroups.toString()], status : 403]
    }*/

    //SecUserSecRole.removeAll(user)  //should we do that ? maybe we should create RemoveSecRole command and make a transaction
    user.delete();
    return [data : [success : true,callback : "Cytomine.Views.User.reload()", message: messageSource.getMessage('be.cytomine.DeleteUserCommand', [username] as Object[], Locale.ENGLISH)], status : 200]
  }

  def undo() {
    def userData = JSON.parse(data)
    User user = new User(userData)
    user.id = userData.id
    def username = user.username
    user.save()
    def callback =  "Cytomine.Views.User.reload()"
    def message = messageSource.getMessage('be.cytomine.AddUserCommand', [username] as Object[], Locale.ENGLISH)
    return [data : [success : true, callback : callback, message: message], status : 200]
  }

  def redo() {
    def postData = JSON.parse(postData)
    User user = User.findById(postData.id)
    def username = user.username
    user.delete();
    def callback =  "Cytomine.Views.User.reload()"
    def message = messageSource.getMessage('be.cytomine.DeleteUserCommand', [username] as Object[], Locale.ENGLISH)
    return [data : [success : true, callback : callback, message: message], status : 200]

  }
}
