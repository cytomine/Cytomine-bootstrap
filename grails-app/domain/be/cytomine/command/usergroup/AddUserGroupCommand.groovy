package be.cytomine.command.usergroup

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.UserGroup

class AddUserGroupCommand extends AddCommand implements SimpleCommand {

    def domainService

    def execute() {
        //Init new domain object
        UserGroup domain = UserGroup.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain.user,domain.group])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

}
