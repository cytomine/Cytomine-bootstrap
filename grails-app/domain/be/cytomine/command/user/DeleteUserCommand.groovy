package be.cytomine.command.user

import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import be.cytomine.security.SecUserSecRole
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class DeleteUserCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")

    def postData = JSON.parse(postData)

    User user = User.findById(postData.id)
    log.debug("User="+user)
    if (!user) {
      log.error("User not found with id: " + postData.id)
      return [data : [success : false, errors : "User not found with id: " + postData.id], status : 404]
    }
    data = user.encodeAsJSON()
    def username = user.username
    /*def userGroups = UserGroup.findAllByUser(user)
    if (userGroups != null && userGroups.size() > 0) {
      return [data : [success : false, errors : "User is in one or more groups: " + userGroups.toString()], status : 403]
    }*/

    //SecUserSecRole.removeAll(user)  //should we do that ? maybe we should create RemoveSecRole command and make a transaction
    try {
      log.debug("User will be deleted")
      user.delete(flush:true)

      return [data : [success : true,callback : "Cytomine.Views.User.reload()", message: messageSource.getMessage('be.cytomine.DeleteUserCommand', [username] as Object[], Locale.ENGLISH)], status : 200]
    } catch(org.springframework.dao.DataIntegrityViolationException e)
    {
      log.error(e)
      return [data : [success : false, errors : "User has still data (image, annotation,...)"], status : 400]
    }
  }

  def undo() {
    def userData = JSON.parse(data)
    User user = User.createUserFromData(userData)
    user.id = userData.id
    def username = user.username
    user.save(flush:true)
    log.info "Save user " +   user.id
    def callback =  "Cytomine.Views.User.reload()"
    def message = messageSource.getMessage('be.cytomine.AddUserCommand', [username] as Object[], Locale.ENGLISH)
    return [data : [success : true, callback : callback, message: message], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    User user = User.findById(postData.id)
    def username = user.username
    log.info "Delete user " +  user.id
    user.delete(flush:true);
    def callback =  "Cytomine.Views.User.reload()"
    def message = messageSource.getMessage('be.cytomine.DeleteUserCommand', [username] as Object[], Locale.ENGLISH)
    return [data : [success : true, callback : callback, message: message], status : 200]

  }
}
