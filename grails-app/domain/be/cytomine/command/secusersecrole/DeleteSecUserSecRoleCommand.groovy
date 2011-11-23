package be.cytomine.command.secusersecrole

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User

class DeleteSecUserSecRoleCommand extends DeleteCommand implements SimpleCommand {

    def execute() {

        //Retrieve domain
        User user = User.read(json.user)
        SecRole role = SecRole.read(json.role)
        SecUserSecRole domain = SecUserSecRole.findBySecUserAndSecRole(user, role)
        if (!domain) throw new WrongArgumentException("UserRole $user/$role was not found!")
        //Build response message
        String message = createMessage(domain, [user.id, role.id])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }
}
