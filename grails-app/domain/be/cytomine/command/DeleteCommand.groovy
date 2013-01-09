package be.cytomine.command

import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The DeleteCommand class is a command that delete a domain
 * It provide an execute method that delete domain from command, an undo method that re-build domain and an redo method that delete domain
 */
class DeleteCommand extends Command {

    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Retrieve domain to delete it
        def oldDomain = service.retrieve(json)
        //Create a backup (for 'undo' op)
        def backup = oldDomain.encodeAsJSON()
        //Init command info
        super.setProject(oldDomain?.projectDomain())
        def response = service.destroy(oldDomain, printMessage)
        fillCommandInfoJSON(backup, response.data.message)
        return response
    }

    /**
     * Process an undo op
     * @return Message
     */
    def undo() {
        initService()
        return service.create(JSON.parse(data), printMessage)
    }

    /**
     * Process a redo op
     * @return Message
     */
    def redo() {
        initService()
        return service.destroy(JSON.parse(data), printMessage)
    }
}
