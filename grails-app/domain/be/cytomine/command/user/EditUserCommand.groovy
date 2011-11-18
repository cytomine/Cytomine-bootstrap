package be.cytomine.command.user

import be.cytomine.security.User
import grails.converters.JSON

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.EditCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.Exception.CytomineException

class EditUserCommand extends EditCommand implements UndoRedoCommand {

    def execute() {
        log.info "Execute"
        User updatedUser=null
        try {
            def postData = JSON.parse(postData)
            updatedUser = User.get(postData.id)
            return super.validateAndSave(postData,updatedUser,[updatedUser.id,updatedUser.username] as Object[])
        } catch(NullPointerException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 404]
        } catch(ConstraintException e) {
            log.error(e)
            return [data : [success : false, errors : updatedUser.retrieveErrors()], status : 400]
        } catch(IllegalArgumentException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 400]
        } catch(CytomineException ex){
      return [data : [image:null,errors:["Cannot save image:"+ex.toString()]], status : 400]
    }

    }

    def undo() {
        log.info "Undo"
        def userData = JSON.parse(data)
        User user = User.findById(userData.previousUser.id)
        user = User.getFromData(user,userData.previousUser)
        user.save(flush:true)
        super.createUndoMessage(userData, user, [user.id,user.username] as Object[])
    }

    def redo() {
        log.info "Redo"
        def userData = JSON.parse(data)
        User user = User.findById(userData.newUser.id)
        user = User.getFromData(user, userData.newUser)
        user.save(flush:true)
        super.createRedoMessage(userData, user, [user.id,user.username] as Object[])
    }
}
