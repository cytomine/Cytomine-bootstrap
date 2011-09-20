package be.cytomine.command.secusersecrole

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.security.SecUserSecRole
import grails.converters.JSON
import java.util.prefs.BackingStoreException
import be.cytomine.security.User
import be.cytomine.security.SecRole

class DeleteSecUserSecRoleCommand extends DeleteCommand implements UndoRedoCommand {

    def execute() {
        try {
            def postData = JSON.parse(postData)
            User user = User.read(postData.user)
            SecRole role = SecRole.read(postData.role)
            SecUserSecRole userRole = SecUserSecRole.findBySecUserAndSecRole(user, role)
            return super.deleteAndCreateDeleteMessage(user.id,userRole,[user.id,role.id] as Object[])
        } catch(NullPointerException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 404]
        } catch(BackingStoreException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 400]
        }
    }

    def undo() {

    }

    def redo() {

    }
}
