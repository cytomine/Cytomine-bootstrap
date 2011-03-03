package be.cytomine.command.user

import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class AddUserCommand extends Command implements UndoRedoCommand {

  def execute() {
    def userData = JSON.parse(postData)
    def newUser = User.createUserFromData(userData.user)

    if (newUser.validate()) {
      newUser.save()
      data = newUser.encodeAsJSON()
      def callback =  "Cytomine.Views.User.reload()"
      def message = messageSource.getMessage('be.cytomine.AddUserCommand', [newUser.username] as Object[], Locale.ENGLISH)
      return [data : [success : true, message: message, user : newUser, callback : callback], status : 201]
    } else {
      return [data : [user : newUser, errors : newUser.retrieveErrors()], status : 400]
    }
  }

  def undo() {
    def userData = JSON.parse(data)
    def user = User.findById(userData.id)
    log.debug("Delete user with id:"+userData.id)
    user.delete(flush:true)
    def callback =  "Cytomine.Views.User.reload()"
    def message = messageSource.getMessage('be.cytomine.DeleteUserCommand', [userData.username] as Object[], Locale.ENGLISH)
    return [data : [callback : callback , message: message], status : 200]
  }

  def redo() {
    def userData = JSON.parse(data)
    def user = User.createUserFromData(userData)
    user.id = userData.id
    user.save(flush:true)
    def callback =  "Cytomine.Views.User.reload()"
    def message = messageSource.getMessage('be.cytomine.AddUserCommand', [user.username] as Object[], Locale.ENGLISH)
    return [data : [user : user, callback : callback, message : message], status : 201]
  }

}
