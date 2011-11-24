package be.cytomine.command

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import grails.converters.JSON
import java.util.prefs.BackingStoreException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
class DeleteCommand extends Command {

    static String commandNameUndo = "Add"
    static String commandNameRedo = "Delete"

    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Delete")
    }

    protected def restore(def service,def json) {
        return service.restore(json,commandNameUndo,printMessage)
    }

    protected def destroy(def service,def json) {
        return service.destroy(json,commandNameRedo,printMessage)
    }
}
