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
    // String actiontype = "DELETE"
    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Delete")
    }

    public def createResponseMessageUndo(def object, def messageParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Add",null)
    }
    public def createResponseMessageUndo(def object, def messageParams,def additionalCallbackParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Add",additionalCallbackParams)
    }
    public def createResponseMessageRedo(def object, def messageParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Delete",null)
    }
    public def createResponseMessageRedo(def object, def messageParams,def additionalCallbackParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Delete",additionalCallbackParams)
    }

    /**
     * Create undo message for an undo of a delete on newObject
     * @param newObject Object that has been undo-deleted
     * @param messageParams Params for the result message
     * @return Result message
     */
    def createUndoMessage(def newObject, Object[] messageParams) {
        log.info "createUndoMessage"
        this.createUndoMessage(newObject, messageParams, null);
    }

    /**
     * Create undo message for an undo of a delete on newObject
     * @param newObject Object that has been undo-deleted
     * @param messageParams Params for the result message
     * @param additionalCallbackParams Aditionnal CallBack params (like ImageID for an annotation)
     * @return Result message
     */
    def createUndoMessage(def newObject, Object[] messageParams, HashMap<String, Object> additionalCallbackParams) {
        String objectName = getClassName(newObject)
        log.info("Undo DeleteCommand " + objectName)

        //save new id of the object that has been re-created
        def postDataLocal = JSON.parse(postData)
        postDataLocal.id = newObject.id
        postData = postDataLocal.toString()

        String command = "be.cytomine.Add" + objectName + "Command"

        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

        log.debug("Add " + objectName + " with id:" + newObject.id)

        HashMap<String, Object> paramsCallback = new HashMap<String, Object>()
        paramsCallback.put('method', command)
        paramsCallback.put(idName, newObject.id)
        if (additionalCallbackParams)
            paramsCallback.putAll(additionalCallbackParams);

        def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)

        HashMap<String, Object> params = new HashMap<String, Object>()
        params.put('message', message)
        params.put('callback', paramsCallback)
        params.put('printMessage', printMessage)
        params.put(objectName.toLowerCase(), id)

        return [data: params, status: 200]
    }

    /**
     * Create redo message for a redo of a delete on newObject
     * @param id newObject id
     * @param newObject Object that has the same type as newObject (just to have it's class name)
     * @param messageParams Params for the result message
     * @return Result message
     */
    def createRedoMessage(String id, def object, Object[] messageParams) {
        this.createRedoMessage(id, object, messageParams, null)
    }

    /**
     * Create redo message for a redo of a delete on newObject
     * @param id newObject id
     * @param newObject Object that has the same type as newObject (just to have it's class name)
     * @param messageParams Params for the result message
     * @param additionalCallbackParams Aditionnal CallBack params (like ImageID for an annotation)
     * @return Result message
     */
    def createRedoMessage(String id, def object, Object[] messageParams, HashMap<String, Object> additionalCallbackParams) {
        log.info("Redo:" + data.replace("\n", ""))
        String objectName = getClassName(object)
        String command = "be.cytomine.Delete" + objectName + "Command"

        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

        log.debug("Delete object:" + id)

        HashMap<String, Object> paramsCallback = new HashMap<String, Object>()
        paramsCallback.put('method', command)
        paramsCallback.put(idName, id)
        if (additionalCallbackParams)
            paramsCallback.putAll(additionalCallbackParams);

        def message = messageSource.getMessage(command, messageParams, Locale.ENGLISH)


        HashMap<String, Object> params = new HashMap<String, Object>()
        params.put('message', message)
        params.put('callback', paramsCallback)
        params.put('printMessage', printMessage)
        params.put(objectName.toLowerCase(), id)

        def result = [data: params, status: 200];

        return result
    }


}
