package be.cytomine.command

import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:43
 */
class DeleteCommand extends Command {

    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Delete")
    }

    def undo() {
        initService()
        return service.restore(JSON.parse(data),printMessage)
    }

    def redo() {
        initService()
        return service.destroy(JSON.parse(data),printMessage)
    }

    def execute()  {
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
}
