package be.cytomine.command

import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The AddCommand class is a command that create a new domain
 * It provide an execute method that create domain from command, an undo method that drop domain and an redo method that recreate domain
 */
class AddCommand extends Command {

    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Create new domain from json data
        def newDomain = service.createFromJSON(json)
        //Save new domain in database
        def response = service.create(newDomain, printMessage)
        //Init command domain
        newDomain = response.object
        fillCommandInfo(newDomain, response.data.message)
        super.setProject(newDomain?.projectDomain())
        return response
    }

    /**
     * Process an undo op
     * @return Message
     */
    def undo() {
        initService()
        return service.destroy(JSON.parse(data), printMessage)
    }

    /**
     * Process a redo op
     * @return Message
     */
    def redo() {
        initService()
        return service.create(JSON.parse(data), printMessage)
    }
}




