package be.cytomine.command

import grails.converters.JSON

/**
 * The DeleteCommand class is a command that delete a domain
 * It provide an execute method that delete domain from command, an undo method that re-build domain and an redo method that delete domain
 * @author ULG-GIGA Cytomine Team
 */
class DeleteCommand extends Command {

    def backup
    /**
     * Add project link in command
     */
    boolean linkProject = true


    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Retrieve domain to delete it
        def oldDomain = domain
        //Init command info
        super.setProject(linkProject? oldDomain?.projectDomain() : null)
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
