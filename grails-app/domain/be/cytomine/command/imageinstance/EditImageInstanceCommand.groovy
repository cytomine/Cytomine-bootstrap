package be.cytomine.command.imageinstance

import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import grails.converters.JSON
import be.cytomine.image.ImageInstance

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class EditImageInstanceCommand extends EditCommand implements UndoRedoCommand  {


  def execute() {

    try
    {
      log.info "Execute"
      log.debug "postData="+postData
      def postData = JSON.parse(postData)

      log.debug "Image instance id="+postData.id
      def updatedImage = ImageInstance.get(postData.id)
      def backup = updatedImage.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

      if (!updatedImage ) {
        log.error "Image instance not found with id: " + postData.id
        return [data : [success : false, message : "Image instance not found with id: " + postData.id], status : 404]
      }
      log.info "getImageInstanceFromData:"+postData
      updatedImage = ImageInstance.getImageInstanceFromData(updatedImage,postData)
      updatedImage.id = postData.id

      log.info "updatedImage.id=" + updatedImage.id

      if ( updatedImage.validate() && updatedImage.save(flush:true)) {
        log.info "New image instance is saved"
        data = ([ previousImageInstance : (JSON.parse(backup)), newImageInstance :  updatedImage]) as JSON
        return [data : [success : true, message:"ok", imageinstance :  updatedImage], status : 200]
      } else {
        log.error "New image can't be saved: " +  updatedImage.errors
        return [data : [imageinstance :  updatedImage, errors : updatedImage.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException e)
    {
      log.error "New image can't be saved: " +  e.toString()
      return [data : [imageinstance : null , errors : [e.toString()]], status : 400]
    }


  }

  def undo() {
    log.info "Undo"
    def imageData = JSON.parse(data)
    ImageInstance image = ImageInstance.findById(imageData.previousImage.id)
    image = ImageInstance.getImageInstanceFromData(image,imageData.previousImage)
    image.save(flush:true)
    return [data : [success : true, message:"ok", imageinstance : image], status : 200]
  }

  def redo() {
    log.info "Redo"
    def imageData = JSON.parse(data)
    ImageInstance image = ImageInstance.findById(imageData.newImage.id)
    image = ImageInstance.getImageInstanceFromData(image,imageData.newImage)
    image.save(flush:true)
    return [data : [success : true, message:"ok", imageinstance : image], status : 200]
  }
}
