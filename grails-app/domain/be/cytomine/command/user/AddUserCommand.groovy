package be.cytomine.command.user

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.security.User
import grails.converters.JSON

class AddUserCommand extends AddCommand implements UndoRedoCommand {

    def domainService

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
        log.info("Undo")
        def userData = JSON.parse(data)
        def user = User.findById(userData.id)
        user.delete(flush: true)
        String id = userData.id
        return super.createUndoMessage(id, user, [userData.id, userData.username] as Object[]);
    }

    def redo() {
        log.info("Redo")
        def userData = JSON.parse(data)
        def user = User.createFromData(userData)
        user.id = userData.id
        user.save(flush: true)
        return super.createRedoMessage(user, [userData.id, userData.username] as Object[]);
    }
}
