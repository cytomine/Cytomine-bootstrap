package be.cytomine.command.user

import grails.converters.JSON
import be.cytomine.security.User

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException
import be.cytomine.Exception.CytomineException

class DeleteUserCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    log.info("Execute")

    try {
      def postData = JSON.parse(postData)
      User user = User.findById(postData.id)
      return super.deleteAndCreateDeleteMessage(postData.id,user,[user.id,user.username] as Object[])
    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(BackingStoreException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    } catch(CytomineException ex){
      return [data : [image:null,errors:["Cannot save image:"+ex.toString()]], status : 400]
    }
  }



  def undo() {
    log.info("Undo")
    def userData = JSON.parse(data)
    User user = User.createFromData(userData)
    user.id = userData.id
    user.save(flush:true)
    log.error "User errors = " + user.errors
    return super.createUndoMessage(user,[user.id,user.username] as Object[]);
  }



  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    User user = User.findById(postData.id)
    String username = user.username
    user.delete(flush:true);
    String id = postData.id
    return super.createRedoMessage(id,user[id,username] as Object[]);
  }
}
