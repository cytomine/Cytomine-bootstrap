package be.cytomine.command.user

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.security.User
import grails.converters.JSON

class AddUserCommand extends AddCommand implements UndoRedoCommand {

    def execute() {
        //Init new domain object
        User domain = User.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain.username])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return destroy(userService,JSON.parse(data))
    }

    def redo() {
        return restore(userService,JSON.parse(data))
    }
}
