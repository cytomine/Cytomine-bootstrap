package be.cytomine.command.image

import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Image
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class EditImageCommand extends Command implements UndoRedoCommand  {


  def execute() {

    try
    {
      log.info "Execute"
      log.debug "postData="+postData
      def postData = JSON.parse(postData)

      log.debug "Image id="+postData.image.id
      def updatedImage = Image.get(postData.image.id)
      def backup = updatedImage.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

      if (!updatedImage ) {
        log.error "Image not found with id: " + postData.image.id
        return [data : [success : false, message : "Image not found with id: " + postData.image.id], status : 404]
      }
      log.info "getImageFromData:"+postData.image
      updatedImage = Image.getImageFromData(updatedImage,postData.image)
      updatedImage.id = postData.image.id

      log.info "updatedImage.id=" + updatedImage.id
      log.info "updatedImage.filename=" + updatedImage.filename

      if ( updatedImage.validate() && updatedImage.save(flush:true)) {
        log.info "New image is saved"
        data = ([ previousImage : (JSON.parse(backup)), newImage :  updatedImage]) as JSON
        return [data : [success : true, message:"ok", image :  updatedImage], status : 200]
      } else {
        log.error "New image can't be saved: " +  updatedImage.errors
        return [data : [image :  updatedImage, errors : updatedImage.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException e)
    {
      log.error "New image can't be saved: " +  e.toString()
      return [data : [image : null , errors : [e.toString()]], status : 400]
    }


  }

  def undo() {
    log.info "Undo"
    def imageData = JSON.parse(data)
    Image image = Image.findById(imageData.previousImage.id)
    image = Image.getImageFromData(image,imageData.previousImage)
    image.save(flush:true)
    return [data : [success : true, message:"ok", image : image], status : 200]
  }

  def redo() {
    log.info "Redo"
    def imageData = JSON.parse(data)
    Image image = Image.findById(imageData.newImage.id)
    image = Image.getImageFromData(image,imageData.newImage)
    image.save(flush:true)
    return [data : [success : true, message:"ok", image : image], status : 200]
  }
}
