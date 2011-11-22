package be.cytomine.command.secusersecrole

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.security.SecUserSecRole
import grails.converters.JSON
import java.util.prefs.BackingStoreException
import be.cytomine.security.User
import be.cytomine.security.SecRole
import be.cytomine.command.SimpleCommand
import be.cytomine.Exception.WrongArgumentException

class DeleteSecUserSecRoleCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        User user = User.read(json.user)
        SecRole role = SecRole.read(json.role)
        SecUserSecRole userRole = SecUserSecRole.findBySecUserAndSecRole(user, role)
        if (!userRole) throw new WrongArgumentException("UserRole $user/$role was not found!")
        return super.deleteAndCreateDeleteMessage(user.id, userRole, [user.id, role.id] as Object[])
    }
}
