package be.cytomine.command

import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.validation.Errors
import org.codehaus.groovy.grails.exceptions.GrailsException
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
class AddCommand extends Command {
  // String actiontype = "ADD"

  def validateAndSave(def newObject,String objectName,Object[] messageParams) throws ValidationException {
    log.info("validateAndSave")

    String command = "be.cytomine.Add" + objectName +"Command"

    if (newObject.validate()) {
      newObject.save(flush:true)
      log.info("Save object with id:"+newObject.id)
      data = newObject.encodeAsJSON()

      if(messageParams[0].equals("#ID#"))
        messageParams[0] = newObject.id

      def message = messageSource.getMessage(command,messageParams as Object[], Locale.ENGLISH)
      actionMessage = message

      HashMap<String,Object> params = new HashMap<String,Object>()
      params.put('success',true)
      params.put('message',message)
      params.put(objectName.toLowerCase(),newObject)


      return [data : params, status : 201]
    } else throw new ConstraintException()
  }


  def undo(def jsonData, def object,String objectName,Object[] messageParams) {
      this.undo(jsonData,object,objectName,messageParams,null);
  }

  /**
   * Undo command
   * @param jsonData json data for the object that must be deleted
   * @param object object that must have the same type as the main object
   * @param command  string with command type
   * @param messageParams params for the message builder (see i18n file)
   * @return json with message, object id, callback, response code...
   */
  def undo(def jsonData, def object,String objectName, Object[] messageParams, HashMap<String,Object> additionalCallbackParams) {
    log.info("Undo AddCommand "+object.getClass().name)

    String command = "be.cytomine.Delete" + objectName +"Command"

    String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

    def o = object.get(jsonData.id)
    o.delete(flush:true)

    log.debug("Delete "+ o.getClass().name + " with id:"+jsonData.id)
    log.debug(jsonData)

    HashMap<String,Object> paramsCallback = new HashMap<String,Object>()
    paramsCallback.put('method',command)
    paramsCallback.put(idName,jsonData.id)
    if(additionalCallbackParams)
      paramsCallback.putAll(additionalCallbackParams);

    def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)

    HashMap<String,Object> params = new HashMap<String,Object>()
    params.put('message',message)
    params.put('callback',paramsCallback)
    params.put(objectName.toLowerCase(),o.id)

    return [data : params, status : 200]
  }

  def redo(def jsonData, def jsonObject, def object, String objectName, Object[] messageParams) {
      this.redo(jsonData,jsonObject,object,objectName,messageParams,null)
  }

  def redo(def jsonData, def jsonObject, def object, String objectName, Object[] messageParams,HashMap<String,Object> additionalCallbackParams) {
    log.info("Redo:"+data.replace("\n",""))

    String command = "be.cytomine.Add" + objectName +"Command"

    String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

    def o = object.createFromData(jsonObject)
    o.id = jsonData.id
    o.save(flush:true)
    log.debug("Save object:"+o.id)

    HashMap<String,Object> paramsCallback = new HashMap<String,Object>()
    paramsCallback.put('method',command)
    paramsCallback.put(idName,jsonData.id)
    if(additionalCallbackParams)
      paramsCallback.putAll(additionalCallbackParams);

    def message = messageSource.getMessage(command, messageParams, Locale.ENGLISH)


    HashMap<String,Object> params = new HashMap<String,Object>()
    params.put('message',message)
    params.put('callback',paramsCallback)
    params.put(objectName.toLowerCase(),o)

    def result = [data : params, status : 201];

    return result
  }

}




