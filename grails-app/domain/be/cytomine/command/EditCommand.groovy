package be.cytomine.command

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
class EditCommand extends Command {

    static String commandNameUndo = "Edit"
    static String commandNameRedo = "Edit"

    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Edit")
    }

    protected def edit(def service, def json) {
        return service.edit(json,commandNameRedo,printMessage)
    }

    protected void fillCommandInfo(def newObject,def oldObject, String message) {
        HashMap<String, Object> paramsData = new HashMap<String, Object>()
        paramsData.put('previous' + responseService.getClassName(newObject), (JSON.parse(oldObject)))
        paramsData.put("new" + responseService.getClassName(newObject), newObject)
        data = (paramsData) as JSON
        actionMessage = message
    }

    protected def fillDomainWithData(def object, def json)
    {
        def domain = object.get(json.id)
        domain = object.getFromData(domain,json)
        domain.id = json.id
        return domain
    }

}
