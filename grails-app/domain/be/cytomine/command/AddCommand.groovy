package be.cytomine.command

import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The AddCommand class is a command that create a new domain
 * It provide an execute method that create domain from command, an undo method that drop domain and an redo method that recreate domain
 */
class AddCommand extends Command {

    /**
     * Return a response message for the domain instance thanks to message parameters un params
     * @param domain Domain instance
     * @param params Message parameters
     * @return Message
     */
    def createMessage(def domain, def params) {
        responseService.createMessage(domain, params, "Add")
    }

    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Create new domain
        log.debug "initService"
        def newDomain = service.createFromJSON(json)
        log.debug "newDomain="+newDomain
        def response = service.create(newDomain, printMessage)
        log.debug "response="+response
        //Init command info
        fillCommandInfo(newDomain, response.data.message)
        super.initCurrentCommantProject(newDomain?.projectDomain())
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




