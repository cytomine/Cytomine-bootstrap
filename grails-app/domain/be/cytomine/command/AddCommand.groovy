package be.cytomine.command

import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:42
 */
class AddCommand extends Command {

    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Add")
    }

    def undo() {
        initService()
        return service.destroy(JSON.parse(data),printMessage)
    }

    def redo() {
        initService()
        return service.restore(JSON.parse(data),printMessage)
    }

    def execute()  {
        initService()
        //Create new domain
        def newDomain = service.createFromJSON(json)
        def response = service.restore(newDomain,printMessage)
        //Init command info
        fillCommandInfo(newDomain, response.message)
        super.initCurrentCommantProject(newDomain?.projectDomain())
        return response
    }
}




