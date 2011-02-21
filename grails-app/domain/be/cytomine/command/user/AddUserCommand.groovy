package be.cytomine.command.user

import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class AddUserCommand extends Command implements UndoRedoCommand {

  def execute() {
    def newUser = User.getUserFromData(JSON.parse(postData))
    if (newUser.validate()) {
      newUser.save()
      data = newUser.encodeAsJSON()
      return [data : [success : true, message:"ok", user : newUser], status : 201]
    } else {
      return [data : [user : newUser, errors : newUser.retrieveErrors()], status : 400]
    }
  }

  def undo() {
    def userData = JSON.parse(data)
    def user = User.findById(userData.id)
    user.delete()
    return [data : null, status : 200]
  }

  def redo() {
    def userData = JSON.parse(data)
    def user = User.getUserFromData(JSON.parse(postData))
    user.id = userData.id
    user.save()
    return [data : [user : user], status : 200]
  }
}
