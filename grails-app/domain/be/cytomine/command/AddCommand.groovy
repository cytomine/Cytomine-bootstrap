package be.cytomine.command

import grails.validation.ValidationException

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

  /**
   * Validate and save "newObject"  and create message with messageParams
   * @param newObject Object that must be check and save (e.g. annotation)
   * @param objectName Class name of the object (e.g. 'Annotation')
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return  Message result
   * @throws ConstraintException Validation fail
   */
  def validateAndSave(def newObject,String objectName,Object[] messageParams) throws ConstraintException {
    log.info("validateAndSave")

    String command = "be.cytomine.Add" + objectName +"Command"

    if (newObject.validate()) {
      newObject.save(flush:true)
      log.info("Save object with id:"+newObject.id)
      data = newObject.encodeAsJSON()

      //replace id if its "#ID#" (not yet done because object is not save before this method
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

  def validateWithoutSave(def newObject,String objectName,Object[] messageParams) throws ConstraintException {
    log.info("validateAndSave")

    String command = "be.cytomine.Add" + objectName +"Command"

    if (newObject.validate()) {
      log.info("Save object with id:"+newObject.id)
      data = newObject.encodeAsJSON()

      //replace id if its "#ID#" (not yet done because object is not save before this method
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

  /**
   * Create an Undo Message for an Add
   * @param id Id of the object that must be undo (e.g. annotation id)
   * @param objectName Class name of the object (e.g. 'Annotation')
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return Undo Message
   */
  def createUndoMessage(String id,String objectName,Object[] messageParams) {
    log.info "createUndoMessage"
      this.createUndoMessage(id,objectName,messageParams,null);
  }

  /**
   * Create an Undo Message for an Add
   * @param id Id of the object that must be undo (e.g. annotation id)
   * @param objectName Class name of the object (e.g. 'Annotation')
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @param additionalCallbackParams Additionnal params for the callbak part of the response (e.g. imageID for an annotation)
   * @return Undo Message
   */
  def createUndoMessage(String id,String objectName, Object[] messageParams, HashMap<String,Object> additionalCallbackParams) {
    log.info("Undo AddCommand "+objectName)

    String command = "be.cytomine.Delete" + objectName +"Command"

    String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

    log.debug("Delete "+ objectName + " with id:"+id)

    HashMap<String,Object> paramsCallback = new HashMap<String,Object>()
    paramsCallback.put('method',command)
    paramsCallback.put(idName,id)
    if(additionalCallbackParams)
      paramsCallback.putAll(additionalCallbackParams);

    def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)

    HashMap<String,Object> params = new HashMap<String,Object>()
    params.put('message',message)
    params.put('callback',paramsCallback)
    params.put(objectName.toLowerCase(),id)

    return [data : params, status : 200]
  }

  /**
   * Create an Redo Message for an Add
   * @param object Object that must be redo (e.g. annotation)
   * @param objectName Class name of the object (e.g. 'Annotation')
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return Redo Message
   */
  def createRedoMessage(def object, String objectName, Object[] messageParams) {
      this.createRedoMessage(object,objectName,messageParams,null)
  }

  /**
   * Create an Redo Message for an Add
   * @param object Object that must be redo (e.g. annotation)
   * @param objectName Class name of the object (e.g. 'Annotation')
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @param additionalCallbackParams Additionnal params for the callbak part of the response (e.g. imageID for an annotation)
   * @return Redo Message
   */
  def createRedoMessage(def object, String objectName, Object[] messageParams,HashMap<String,Object> additionalCallbackParams) {
    log.info("Redo:"+data.replace("\n",""))

    String command = "be.cytomine.Add" + objectName +"Command"

    String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

    log.debug("Save object:"+object.id)

    HashMap<String,Object> paramsCallback = new HashMap<String,Object>()
    paramsCallback.put('method',command)
    paramsCallback.put(idName,object.id)
    if(additionalCallbackParams)
      paramsCallback.putAll(additionalCallbackParams);

    def message = messageSource.getMessage(command, messageParams, Locale.ENGLISH)


    HashMap<String,Object> params = new HashMap<String,Object>()
    params.put('message',message)
    params.put('callback',paramsCallback)
    params.put(objectName.toLowerCase(),object)

    def result = [data : params, status : 201];

    return result
  }
}




