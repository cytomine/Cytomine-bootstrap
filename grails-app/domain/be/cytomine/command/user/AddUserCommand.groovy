package be.cytomine.command.user

import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddUserCommand extends AddCommand implements UndoRedoCommand {



  def execute() {
    log.info("Execute")
    User newUser
    try {
      def json = JSON.parse(postData)
      newUser = User.createUserFromData(json)
      return super.validateAndSave(newUser,"User",["#ID#",newUser.username] as Object[])
      //errors:
    }catch(ConstraintException  ex){
      return [data : [user:newUser,errors:newUser.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [user:null,errors:["Cannot save user:"+ex.toString()]], status : 400]
    }

  }

  def undo() {
    log.info("Undo")
    def userData = JSON.parse(data)
    def user = User.findById(userData.id)
    log.debug("Delete user with id:"+userData.id)
    user.delete(flush:true)

    log.info ("userData="+userData)

   String id = userData.id

    return super.createUndoMessage(
            id,
            'Term',
            [userData.id,userData.username] as Object[]
    );
  }

  def redo() {
    log.info("Undo")
    def userData = JSON.parse(data)
    def user = User.createUserFromData(userData)
    user.id = userData.id
    user.save(flush:true)
    return super.createRedoMessage(
            user,
            'User',
            [userData.id,userData.username] as Object[]
    );
  }
}
