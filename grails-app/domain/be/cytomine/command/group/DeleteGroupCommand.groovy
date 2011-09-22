package be.cytomine.command.group

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import java.util.prefs.BackingStoreException
import be.cytomine.security.Group
import grails.converters.JSON
import be.cytomine.command.DeleteCommand
import be.cytomine.command.SimpleCommand

class DeleteGroupCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        log.info("Execute")
        try {
            def postData = JSON.parse(postData)
            Group group = Group.findById(postData.id)
            return super.deleteAndCreateDeleteMessage(postData.id,group,[group.id,group.name] as Object[])
        } catch(NullPointerException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 404]
        } catch(BackingStoreException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 400]
        }
    }
}
