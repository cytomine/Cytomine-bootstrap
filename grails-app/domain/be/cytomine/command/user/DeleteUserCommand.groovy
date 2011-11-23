package be.cytomine.command.user

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.security.User
import grails.converters.JSON

class DeleteUserCommand extends DeleteCommand implements UndoRedoCommand {

    def execute() {
        User user = User.findById(json.id)
        if (!user) throw new ObjectNotFoundException("User $json.id was not found")
        return super.deleteAndCreateDeleteMessage(json.id, user, [user.id, user.username] as Object[])
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
