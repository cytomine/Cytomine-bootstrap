package be.cytomine.command.user

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.security.User
import grails.converters.JSON

class AddUserCommand extends AddCommand implements UndoRedoCommand {

    def execute() {
        User newUser = User.createFromData(json)
        return super.validateAndSave(newUser, ["#ID#", newUser.username] as Object[])
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
