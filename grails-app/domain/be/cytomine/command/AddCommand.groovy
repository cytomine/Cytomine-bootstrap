package be.cytomine.command

import be.cytomine.Exception.ConstraintException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
class AddCommand extends Command {
    protected createMessage(def updatedTerm, def params) {
        responseService.createMessage(updatedTerm, params, "Add")
    }
    public def createResponseMessageUndo(def object, def messageParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Delete",null)
    }
    public def createResponseMessageUndo(def object, def messageParams,def additionalCallbackParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Delete",additionalCallbackParams)
    }
    public def createResponseMessageRedo(def object, def messageParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Add",null)
    }
    public def createResponseMessageRedo(def object, def messageParams,def additionalCallbackParams) {
        responseService.createResponseMessage(object,messageParams,printMessage,"Add",additionalCallbackParams)
    }

    /**
     * Create an Undo Message for an Add
     * @param id Id of the object that must be undo (e.g. annotation id)
     * @param object Object that must have the same type as the "undo-add" object
     * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
     * @return Undo Message
     */
    def createUndoMessage(String id, def object, Object[] messageParams) {
        log.info "createUndoMessage"
        this.createUndoMessage(id, object, messageParams, null);
    }

    /**
     * Create an Undo Message for an Add
     * @param id Id of the object that must be undo (e.g. annotation id)
     * @param object Object that must have the same type as the "undo-add" object
     * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
     * @param additionalCallbackParams Additionnal params for the callbak part of the response (e.g. imageID for an annotation)
     * @return Undo Message
     */
    def createUndoMessage(String id, def object, Object[] messageParams, HashMap<String, Object> additionalCallbackParams) {

        String objectName = getClassName(object)
        log.info("Undo AddCommand " + objectName)
        String command = "be.cytomine.Delete" + objectName + "Command"

        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

        log.debug("Delete " + objectName + " with id:" + id)

        HashMap<String, Object> paramsCallback = new HashMap<String, Object>()
        paramsCallback.put('method', command)
        paramsCallback.put(idName, id)
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
     * Create an Redo Message for an Add
     * @param object Object that must be redo (e.g. annotation)
     * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
     * @return Redo Message
     */
    def createRedoMessage(def object, Object[] messageParams) {
        this.createRedoMessage(object, messageParams, null)
    }

    /**
     * Create an Redo Message for an Add
     * @param object Object that must be redo (e.g. annotation)
     * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
     * @param additionalCallbackParams Additionnal params for the callbak part of the response (e.g. imageID for an annotation)
     * @return Redo Message
     */
    def createRedoMessage(def object, Object[] messageParams, HashMap<String, Object> additionalCallbackParams) {
        log.info("Redo:" + data.replace("\n", ""))
        String objectName = getClassName(object)
        String command = "be.cytomine.Add" + objectName + "Command"

        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

        log.debug("Save object:" + object.id)

        HashMap<String, Object> paramsCallback = new HashMap<String, Object>()
        paramsCallback.put('method', command)
        paramsCallback.put(idName, object.id)
        if (additionalCallbackParams)
            paramsCallback.putAll(additionalCallbackParams);

        def message = messageSource.getMessage(command, messageParams, Locale.ENGLISH)


        HashMap<String, Object> params = new HashMap<String, Object>()
        params.put('message', message)
        params.put('callback', paramsCallback)
        params.put('printMessage', printMessage)
        params.put(objectName.toLowerCase(), object)

        def result = [data: params, status: 200];

        return result
    }
}




