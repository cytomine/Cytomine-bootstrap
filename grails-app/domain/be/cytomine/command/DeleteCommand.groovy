package be.cytomine.command

import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The DeleteCommand class is a command that delete a domain
 * It provide an execute method that delete domain from command, an undo method that re-build domain and an redo method that delete domain
 */
class DeleteCommand extends Command {

    /**
     * Return a response message for the domain instance thanks to message parameters un params
     * @param domain Domain instance
     * @param params Message parameters
     * @return Message
     */
    def createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Delete")
    }

    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Create new domain
        def oldDomain = service.retrieve(json)
        def backup = oldDomain.encodeAsJSON()
        //Init command info
        super.initCurrentCommantProject(oldDomain?.projectDomain())

        def response = service.destroy(oldDomain, printMessage)
        fillCommandInfoJSON(backup, response.message)
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
