package be.cytomine

import grails.converters.JSON

class ResponseService {

    static transactional = true

    def messageSource

    public String createMessage(def newObject, def messageParams, String commandType) {
        String command = getCommandName(newObject, commandType)
        messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)
    }

    public def createResponseMessage(def newObject, String message, boolean printMessage) {
        HashMap<String, Object> params = new HashMap<String, Object>()
        params.put('success', true)
        params.put('message', message)
        params.put('printMessage', printMessage)
        params.put(getClassName(newObject).toLowerCase(), newObject)
        return [data: params, status: 200]
    }

    public def createResponseMessage(def object, def messageParams, boolean printMessage, String commandType) {
        createResponseMessage(object, messageParams, printMessage, commandType, null)
    }

    public def createResponseMessage(def object, def messageParams, boolean printMessage, String commandType, HashMap<String, Object> additionalCallbackParams) {
        String objectName = getClassName(object)
        String command = "be.cytomine." + commandType + objectName + "Command"
        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...
        String id = object.id
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
        params.put(objectName.toLowerCase(), JSON.parse((String)object.encodeAsJSON()))

        return [data: params, status: 200, object:object];
    }


//    public def createResponseMessageDelete(def object, def messageParams, boolean printMessage, String commandType) {
//        createResponseMessage(object, messageParams, printMessage, commandType, null)
//    }
//
//    public def createResponseMessageDelete(def object, def messageParams, boolean printMessage, String commandType, HashMap<String, Object> additionalCallbackParams) {
//        String objectName = getClassName(object)
//        String command = "be.cytomine." + commandType + objectName + "Command"
//        String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...
//        String id = object.id
//        HashMap<String, Object> paramsCallback = new HashMap<String, Object>()
//        paramsCallback.put('method', command)
//        paramsCallback.put(idName, id)
//        if (additionalCallbackParams)
//            paramsCallback.putAll(additionalCallbackParams);
//
//        def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)
//
//        HashMap<String, Object> params = new HashMap<String, Object>()
//        params.put('message', message)
//        params.put('callback', paramsCallback)
//        params.put('printMessage', printMessage)
//        params.put(objectName.toLowerCase(), object.encodeAsJSON())
//
//        return [data: params, status: 200, object:object.encodeAsJSON()];
//    }










    def getCommandName(def newObject, String commandType) {
        String objectName = getClassName(newObject)
        return "be.cytomine." + commandType + objectName + "Command"
    }

    /**
     * Get the class name of an object without package name
     * @param o Object
     * @return Class name (without package) of o
     */
    public static String getClassName(Object o) {
        //log.info("getClassName=" + o.getClass());
        String name = o.getClass()   //be.cytomine.image.Image
        String[] array = name.split("\\.")  //[be,cytomine,image,Image]
        //log.info array.length
        return array[array.length - 1] // Image
    }


}
