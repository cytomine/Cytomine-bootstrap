package be.cytomine.command

import be.cytomine.utils.JSONUtils
import grails.converters.JSON

class ResponseService {

    static transactional = true

    def messageSource

    /**
     * Create response message structure for a command result
     * E.g. if we try to add a new annotation "object"
     * @param object Object updated (add/edit/delete) by command  (e.g. annotation x)
     * @param messageParams Params for i18n message (e.g. annotation id, annotation image id)
     * @param printMessage Flag for client, indicate if client must print or not a confirmation message
     * @param commandType Command type: add, edit or delete
     * @param additionalCallbackParams (optional): call back to append in response for client (e.g. image id as callback, to refresh image view in web UI client)
     * @return Response stucture
     */
    public def createResponseMessage(def object, def messageParams, boolean printMessage, String commandType, HashMap<String, Object> additionalCallbackParams = null) {
        String objectName = getClassName(object)
        String command = "be.cytomine." + commandType + objectName + "Command"
        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...
        String id = object.id
        HashMap<String, Object> paramsCallback = new HashMap<String, Object>()
        paramsCallback.put('method', command)
        paramsCallback.put(idName, id)
        if (additionalCallbackParams) {
            paramsCallback.putAll(additionalCallbackParams)
        }

        //load message from i18n filel
        def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)

        HashMap<String, Object> params = new HashMap<String, Object>()
        params.put('message', message)
        params.put('callback', paramsCallback)
        params.put('printMessage', printMessage)
        params.put(objectName.toLowerCase(), JSON.parse(JSONUtils.toJSONString(object)))     //Project.getDataFromDomain(object)

        return [data: params, status: 200, object:object]
    }

    /**
     * Get the class name of an object without package name
     * @param o Object
     * @return Class name (without package) of o
     */
    public static String getClassName(Object o) {
        String name = o.getClass().name   //be.cytomine.image.Image
        int exeed = name.indexOf("_\$\$_javassist") //if  be.cytomine.image.Image_$$_javassistxxxx...remove all after  _$$
        if (exeed!=-1) {
            name = name.substring(0,exeed)
        }
        String[] array = name.split("\\.")  //[be,cytomine,image,Image]
        //log.info array.length
        return array[array.length - 1] // Image
    }
}
