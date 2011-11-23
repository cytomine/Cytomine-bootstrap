package be.cytomine

class ResponseService {

    static transactional = true

    def messageSource

    String createMessage(def newObject, def messageParams, String commandType) {
        String command = getCommandName(newObject,commandType)
        messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)
    }

    def getCommandName(def newObject, String commandType) {
        String objectName = getClassName(newObject)
        return "be.cytomine."+commandType + objectName + "Command"
    }

    /**
     * Get the class name of an object without package name
     * @param o Object
     * @return Class name (without package) of o
     */
    public String getClassName(Object o) {
        log.info("getClassName=" + o.getClass());
        String name = o.getClass()   //be.cytomine.image.Image
        String[] array = name.split("\\.")  //[be,cytomine,image,Image]
        log.info array.length
        return array[array.length - 1] // Image
    }

    public def createResponseMessage(def newObject, String message, boolean printMessage) {
        HashMap<String, Object> params = new HashMap<String, Object>()
        params.put('success', true)
        params.put('message', message)
        params.put('printMessage', printMessage)
        params.put(getClassName(newObject).toLowerCase(), newObject)
        return [data: params, status: 200]
    }
}
