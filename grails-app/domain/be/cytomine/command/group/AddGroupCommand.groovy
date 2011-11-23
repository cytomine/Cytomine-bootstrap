package be.cytomine.command.group

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.Group

class AddGroupCommand extends AddCommand implements SimpleCommand {

    def domainService

    def execute() {
        //Init new domain object
        Group domain = Group.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }
}
