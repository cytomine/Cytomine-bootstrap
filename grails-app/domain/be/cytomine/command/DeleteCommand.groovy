package be.cytomine.command

import java.util.prefs.BackingStoreException
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 14/04/11
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
class DeleteCommand extends Command {
  // String actiontype = "DELETE"

  /**
   * Create delete message for object that has been deleted
   * @param id Object id
   * @param objectToDelete Object that must have the same type as the deleted object
   * @param messageParams Params for the result message
   * @return Result message
   * @throws NullPointerException Object don't exist
   * @throws BackingStoreException Object cannot be deleted
   */
  def createDeleteMessage(def id, def objectToDelete, Object[] messageParams) throws NullPointerException, BackingStoreException {
    return deleteAndCreateDeleteMessage(id, objectToDelete, messageParams, false)
  }

  /**
   * Create delete message for object and delete them
   * @param id Object id
   * @param objectToDelete Object that must have the same type as the deleted object
   * @param messageParams Params for the result message
   * @return Result message
   * @throws NullPointerException Object don't exist
   * @throws BackingStoreException Object cannot be deleted
   */
  def deleteAndCreateDeleteMessage(def id, def objectToDelete, Object[] messageParams) throws NullPointerException, BackingStoreException {
    return deleteAndCreateDeleteMessage(id, objectToDelete, messageParams, true)
  }

  /**
   * Create delete message for object and delete them if delete is true
   * @param id Object id
   * @param objectToDelete Object that must have the same type as the deleted object
   * @param messageParams Params for the result message
   * @param delete If true object will be deleted
   * @return Result message
   * @throws NullPointerException Object don't exist
   * @throws BackingStoreException Object cannot be deleted
   */
  def deleteAndCreateDeleteMessage(def id, def objectToDelete, Object[] messageParams, boolean delete) throws NullPointerException, BackingStoreException {
    log.info("delete")
    String objectName = getClassName(objectToDelete)
    String command = "be.cytomine.Delete" + objectName + "Command"

    if (!objectToDelete) throw new NullPointerException(objectName + " not found with id:" + id); //404

    data = objectToDelete.encodeAsJSON()

    try {
      if (delete) objectToDelete.delete(flush: true);

      def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)
      actionMessage = message

      HashMap<String, Object> params = new HashMap<String, Object>()
      params.put('success', true)
      params.put('message', message)
      params.put(objectName.toLowerCase(), objectToDelete)

      return [data: params, status: 200]

    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      log.error(e)
      throw new BackingStoreException(objectName + " is still map with data (relation, annotation...):" + e.toString()) //400
    } catch (Exception e) {
      log.error(e)
      throw new BackingStoreException("Unknow error:" + e.toString()) //400
    }

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
    params.put(objectName.toLowerCase(), id)

    return [data: params, status: 201]
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
    params.put(objectName.toLowerCase(), id)

    def result = [data: params, status: 200];

    return result
  }


}
