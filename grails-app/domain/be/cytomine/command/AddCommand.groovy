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

  /**
   * Validate and save "newObject" and create message with messageParams
   * @param newObject Object that must be check and save (e.g. annotation)
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return Result message
   * @throws ConstraintException Validation fail
   */
  def validateAndSave(def newObject,Object[] messageParams) throws ConstraintException {
      return checkConstraint(newObject,messageParams,true)
  }

  /**
   * Validate but don't save "newObject" and create message with messageParams
   * @param newObject Object that must be check (e.g. annotation)
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return Result message
   * @throws ConstraintException Validation fail
   */
  def validateWithoutSave(def newObject,Object[] messageParams) throws ConstraintException {

      return checkConstraint(newObject,messageParams,false)
  }

  /**
   * Check constraint for newObject and save it if save is true
   * @param newObject Object that must be check
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @param save If true, newObject will be save
   * @return  Result message
   * @throws ConstraintException Validation fail
   */
  def checkConstraint(def newObject,Object[] messageParams, boolean save) throws ConstraintException {
    log.info("validateAndSave")
    //get object class name (e.g. 'Annotation') and command name (e.g. 'be.cytomine.AddAnnotationCommand')
    String objectName = getClassName(newObject)
    String command = "be.cytomine.Add" + objectName +"Command"

    if (newObject.validate()) {
      if(save) {
        if(!newObject.save(flush:true)) throw new ConstraintException(newObject.errors.toString())
        log.info("Save object with id:"+newObject.id)
      }
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
    } else throw new ConstraintException(newObject.errors.toString())
  }


  /**
   * Create an Undo Message for an Add
   * @param id Id of the object that must be undo (e.g. annotation id)
   * @param object Object that must have the same type as the "undo-add" object
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return Undo Message
   */
  def createUndoMessage(String id,def object,Object[] messageParams) {
    log.info "createUndoMessage"
      this.createUndoMessage(id,object,messageParams,null);
  }

  /**
   * Create an Undo Message for an Add
   * @param id Id of the object that must be undo (e.g. annotation id)
   * @param object Object that must have the same type as the "undo-add" object
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @param additionalCallbackParams Additionnal params for the callbak part of the response (e.g. imageID for an annotation)
   * @return Undo Message
   */
  def createUndoMessage(String id,def object, Object[] messageParams, HashMap<String,Object> additionalCallbackParams) {

    String objectName = getClassName(object)
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
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @return Redo Message
   */
  def createRedoMessage(def object, Object[] messageParams) {
      this.createRedoMessage(object,messageParams,null)
  }

  /**
   * Create an Redo Message for an Add
   * @param object Object that must be redo (e.g. annotation)
   * @param messageParams Params fo the message (i18n) (e.g. annotation name, filename of the image...)
   * @param additionalCallbackParams Additionnal params for the callbak part of the response (e.g. imageID for an annotation)
   * @return Redo Message
   */
  def createRedoMessage(def object, Object[] messageParams,HashMap<String,Object> additionalCallbackParams) {
    log.info("Redo:"+data.replace("\n",""))
    String objectName = getClassName(object)
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




