package be.cytomine.command

import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
class AddCommand extends Command {

    static String commandNameUndo = "Delete"
    static String commandNameRedo = "Add"

    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Add")
    }

    protected def restore(def service,def json) {
        return service.restore(json,commandNameRedo,printMessage)
    }

    protected def destroy(def service,def json) {
        return service.destroy(json,commandNameUndo,printMessage)
    }


    def undo() {
        initService()
        return service.destroy(JSON.parse(data),commandNameUndo,printMessage)
    }

    def redo() {
        initService()
        return service.restore(JSON.parse(data),commandNameRedo,printMessage)
    }


    def execute()  {
        initService()
        //Create new domain
        def newDomain = service.createFromJSON(json)
        def response = service.restore(newDomain, "Add", printMessage)
        //Init command info
        fillCommandInfo(newDomain, response.message)
        super.initCurrentCommantProject(newDomain?.projectDomain())
        return response
    }

}




