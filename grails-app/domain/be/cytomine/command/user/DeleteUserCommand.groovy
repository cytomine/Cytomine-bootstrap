package be.cytomine.command.user

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.security.User
import grails.converters.JSON

class DeleteUserCommand extends DeleteCommand implements UndoRedoCommand {

    def execute() {
        //Retrieve domain
        User domain = User.get(json.id)
        if (!domain) throw new ObjectNotFoundException("User " + json.id + " was not found")
        //Build response message
        String message = createMessage(domain, [domain.id, domain.username])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        log.info("Undo")
        def userData = JSON.parse(data)
        User user = User.createFromData(userData)
        user.id = userData.id
        user.save(flush: true)
        return super.createUndoMessage(user, [user.id, user.username] as Object[]);
    }



    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        User user = User.findById(postData.id)
        String username = user.username
        user.delete(flush: true);
        String id = postData.id
        return super.createRedoMessage(id, user[id, username] as Object[]);
    }
}
