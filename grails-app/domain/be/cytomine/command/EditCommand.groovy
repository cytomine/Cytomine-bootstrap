package be.cytomine.command

import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The EditCommand class is a command that edit a domain
 * It provide an execute method that edit domain from command, an undo method that undo edit on domain and an redo method edit domain
 */
class EditCommand extends Command {

    /**
     * Return a response message for the domain instance thanks to message parameters un params
     * @param domain Domain instance
     * @param params Message parameters
     * @return Message
     */
    def createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Edit")
    }

    /**
     * Add command info for the new domain concerned by the command
     * @param newObject domain after update
     * @param oldObject domain before update
     * @param message Message build for the command
     */
    protected void fillCommandInfo(def newObject, def oldObject, String message) {
        HashMap<String, Object> paramsData = new HashMap<String, Object>()
        paramsData.put('previous' + responseService.getClassName(newObject), (JSON.parse(oldObject)))
        paramsData.put("new" + responseService.getClassName(newObject), newObject)
        data = (paramsData) as JSON
        actionMessage = message
    }

    /**
     * Add command info for the new domain concerned by the command
     * @param newObject New domain
     * @param message Message build for the command
     */
    protected def fillDomainWithData(def object, def json) {
        def domain = object.get(json.id)
        domain = object.getFromData(domain, json)
        domain.id = json.id
        return domain
    }

    /**
     * Get domain name
     * @return domaine name
     */
    String domainName() {
        String domain = serviceName.replace("Service", "")
        return domain.substring(0, 1).toUpperCase() + domain.substring(1);
    }

    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Create new domain
        def updatedDomain = service.retrieve(json)
        println "updatedDomain.version="+updatedDomain.version
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Init command info
        super.initCurrentCommantProject(updatedDomain?.projectDomain())

        def response = service.edit(updatedDomain, printMessage)
        fillCommandInfo(updatedDomain, oldDomain, response.data.message)
        return response
    }

    /**
     * Process an undo op
     * @return Message
     */
    def undo() {
        initService()
        return service.edit(JSON.parse(data).get("previous" + domainName()), printMessage)
    }

    /**
     * Process a redo op
     * @return Message
     */
    def redo() {
        initService()
        return service.edit(JSON.parse(data).get("new" + domainName()), printMessage)
    }
}
