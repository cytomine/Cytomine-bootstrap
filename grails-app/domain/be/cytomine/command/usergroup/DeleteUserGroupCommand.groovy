package be.cytomine.command.usergroup

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.security.UserGroup

class DeleteUserGroupCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        //Retrieve domain
        User user = User.read(json.user)
        Group group = Group.read(json.group)
        UserGroup domain = UserGroup.findByUserAndGroup(user, group)
        if (!domain) throw new ObjectNotFoundException("Usergroup with user $user and group $group not found")
        //Build response message
        String message = createMessage(domain, [domain.user, domain.group])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }
}