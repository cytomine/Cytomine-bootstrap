package be.cytomine.command.secusersecrole

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User

class DeleteSecUserSecRoleCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        User user = User.read(json.user)
        SecRole role = SecRole.read(json.role)
        SecUserSecRole userRole = SecUserSecRole.findBySecUserAndSecRole(user, role)
        if (!userRole) throw new WrongArgumentException("UserRole $user/$role was not found!")
        return super.deleteAndCreateDeleteMessage(user.id, userRole, [user.id, role.id] as Object[])
    }
}
