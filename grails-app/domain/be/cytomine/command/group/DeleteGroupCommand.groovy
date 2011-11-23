package be.cytomine.command.group

import be.cytomine.command.DeleteCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.Group
import be.cytomine.Exception.ObjectNotFoundException

class DeleteGroupCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        //Retrieve domain
        Group domain = Group.get(json.id)
        if (!domain) throw new ObjectNotFoundException("Group " + json.id + " was not found")
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name])
         //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }
}
