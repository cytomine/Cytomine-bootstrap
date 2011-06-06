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
class DeleteCommand extends Command{
 // String actiontype = "DELETE"

  def createDeleteMessage(def id, def objectToDelete,String objectName,Object[] messageParams) throws NullPointerException,BackingStoreException {
      return deleteAndCreateDeleteMessage(id,objectToDelete,objectName,messageParams,false)
  }
  def deleteAndCreateDeleteMessage(def id, def objectToDelete,String objectName,Object[] messageParams) throws NullPointerException,BackingStoreException {
      return deleteAndCreateDeleteMessage(id,objectToDelete,objectName,messageParams,true)
  }

  def deleteAndCreateDeleteMessage(def id, def objectToDelete,String objectName,Object[] messageParams, boolean delete) throws NullPointerException,BackingStoreException {
    log.info("delete")

    String command = "be.cytomine.Delete" + objectName +"Command"

    if(!objectToDelete) throw new NullPointerException(objectName + " not found with id:"+id); //404

    data = objectToDelete.encodeAsJSON()

    try {
      if(delete) objectToDelete.delete(flush:true);

      def message = messageSource.getMessage(command,messageParams as Object[], Locale.ENGLISH)
      actionMessage = message

      HashMap<String,Object> params = new HashMap<String,Object>()
      params.put('success',true)
      params.put('message',message)
      params.put(objectName.toLowerCase(),objectToDelete)

      return [data : params, status : 200]

    } catch(org.springframework.dao.DataIntegrityViolationException e){
      log.error(e)
      throw new BackingStoreException(objectName+" is still map with data (relation, annotation...):"+e.toString()) //400
    } catch(Exception e){
      log.error(e)
      throw new BackingStoreException("Unknow error:"+e.toString()) //400
    }

  }





  def createUndoMessage(def newObject,String objectName,Object[] messageParams) {
    log.info "createUndoMessage"
      this.createUndoMessage(newObject,objectName,messageParams,null);
  }

  def createUndoMessage(def newObject,String objectName, Object[] messageParams, HashMap<String,Object> additionalCallbackParams) {
    log.info("Undo DeleteCommand "+objectName)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  newObject.id
    postData = postDataLocal.toString()

    String command = "be.cytomine.Add" + objectName +"Command"

    String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

    log.debug("Add "+ objectName + " with id:"+newObject.id)

    HashMap<String,Object> paramsCallback = new HashMap<String,Object>()
    paramsCallback.put('method',command)
    paramsCallback.put(idName,newObject.id)
    if(additionalCallbackParams)
      paramsCallback.putAll(additionalCallbackParams);

    def message = messageSource.getMessage(command, messageParams as Object[], Locale.ENGLISH)

    HashMap<String,Object> params = new HashMap<String,Object>()
    params.put('message',message)
    params.put('callback',paramsCallback)
    params.put(objectName.toLowerCase(),id)

    return [data : params, status : 201]
  }



  def createRedoMessage(String id, String objectName, Object[] messageParams) {
      this.createRedoMessage(id,objectName,messageParams,null)
  }

  def createRedoMessage(String id, String objectName, Object[] messageParams,HashMap<String,Object> additionalCallbackParams) {
    log.info("Redo:"+data.replace("\n",""))

    String command = "be.cytomine.Delete" + objectName +"Command"

    String idName = objectName.toLowerCase() + "ID" //termID, annotationID,...

    log.debug("Delete object:"+id)

    HashMap<String,Object> paramsCallback = new HashMap<String,Object>()
    paramsCallback.put('method',command)
    paramsCallback.put(idName,id)
    if(additionalCallbackParams)
      paramsCallback.putAll(additionalCallbackParams);

    def message = messageSource.getMessage(command, messageParams, Locale.ENGLISH)


    HashMap<String,Object> params = new HashMap<String,Object>()
    params.put('message',message)
    params.put('callback',paramsCallback)
    params.put(objectName.toLowerCase(),id)

    def result = [data : params, status : 200];

    return result
  }






}
