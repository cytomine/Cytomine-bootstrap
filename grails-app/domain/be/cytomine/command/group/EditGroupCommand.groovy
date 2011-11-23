package be.cytomine.command.group

import be.cytomine.command.EditCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.Group
import be.cytomine.Exception.ObjectNotFoundException

class EditGroupCommand extends EditCommand implements SimpleCommand {

    def execute() {
        //Retrieve
        Group updatedDomain = Group.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Group ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain.name])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
    }
}
