package be.cytomine.command.imageinstance

import be.cytomine.command.AddCommand
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
class AddImageInstanceCommand extends AddCommand implements UndoRedoCommand {

  def execute() {
    try
    {
      log.info("Execute")
      def json = JSON.parse(postData)
      json.user = user.id
      ImageInstance newImage = ImageInstance.createImageInstanceFromData(json)
      if(newImage.validate()) {
        newImage.save(flush:true)
        log.info("Save imageinstance with id:"+newImage.id)
        data = newImage.encodeAsJSON()
        return [data : [success : true , message:"ok", imageinstance : newImage], status : 201]
      } else {
        log.error("Cannot save imageinstance:"+newImage.errors)
        return [data : [imageinstance : newImage , errors : newImage.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save imageinstance:"+ex.toString())
      return [data : [imageinstance : null , errors : ["Cannot save imageinstance:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def imageData = JSON.parse(data)
    def image = ImageInstance.get(imageData.id)
    image.delete(flush:true)
    log.debug("Delete image with id:"+imageData.id)
    return [data : [message : "Image Instance successfuly deleted", imageinstance : imageData.id], status : 200]
  }

  def redo() {

    log.info("Redo:"+data.replace("\n",""))
    def imageData = JSON.parse(data)
    def json = JSON.parse(postData)
    def image = ImageInstance.createImageInstanceFromData(json)
    image.id = imageData.id
    image.save(flush:true)
    log.debug("Save image:"+image.id)
    return [data : [imageinstance : image], status : 201]
  }
}
