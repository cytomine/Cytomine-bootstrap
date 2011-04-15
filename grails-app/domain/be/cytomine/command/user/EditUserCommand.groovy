package be.cytomine.command.user

import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.EditCommand

class EditUserCommand extends EditCommand implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    log.debug "postData="+postData
    def postData = JSON.parse(postData)
    def updatedUser = User.get(postData.id)
    def backup = updatedUser.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

    if (!updatedUser ) {
      return [data : [success : false, message : "User not found with id: " + postData.id], status : 404]
    }
    try
    {
    updatedUser = User.getUserFromData(updatedUser,postData)
    updatedUser.id = postData.id

    //encode the password if necessary
    if (updatedUser.password != postData.password)
      postData.password = updatedUser.springSecurityService.encodePassword(postData.password)

    if ( updatedUser.validate() && updatedUser.save(flush:true)) {
      log.info "New User is saved"
      data = ([ previousUser : (JSON.parse(backup)), newUser :  updatedUser]) as JSON

      def callback = [method : "be.cytomine.EditUserCommand"]
      def message = messageSource.getMessage('be.cytomine.EditUserCommand', [updatedUser.username] as Object[], Locale.ENGLISH)
      return [data : [success : true, message: message, callback: callback, user :  updatedUser], status : 200]
    } else {
      log.error "New User can't be saved: " +  updatedUser.errors
      return [data : [user :  updatedUser, message :  updatedUser.retrieveErrors()], status : 400]
    }
    }
    catch(IllegalArgumentException e)
    {
      log.error "New User can't be saved: " +  e.toString()
      return [data : [user : null , errors : [e.toString()]], status : 400]
    }

  }

  def undo() {
    log.info "Undo"
    def userData = JSON.parse(data)
    User user = User.findById(userData.previousUser.id)
    user = User.getUserFromData(user,userData.previousUser)
    user.save(flush:true)
    def callback = [method : "be.cytomine.EditUserCommand"]
    def message = messageSource.getMessage('be.cytomine.EditUserCommand', [user.username] as Object[], Locale.ENGLISH)
    return [data : [success : true, message: message, callback: callback,user : user], status : 200]
  }

  def redo() {
    log.info "Redo"
    def userData = JSON.parse(data)
    User user = User.findById(userData.newUser.id)
    user = User.getUserFromData(user, userData.newUser)
    user.save(flush:true)
    def callback = [method : "be.cytomine.EditUserCommand"]
    def message = messageSource.getMessage('be.cytomine.EditUserCommand', [user.username] as Object[], Locale.ENGLISH)
    return [data : [success : true, message: message, callback: callback, user : user], status : 200]
  }


}
