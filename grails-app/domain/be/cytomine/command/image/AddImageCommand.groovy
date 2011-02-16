package be.cytomine.command.image

import be.cytomine.security.User
import be.cytomine.project.Image
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class AddImageCommand extends Command implements UndoRedoCommand {

  def execute() {
      log.info("Execute")
      def json = JSON.parse(postData)
      Image newImage = Image.createImageFromData(json.image)
      if(newImage.validate() && newImage.save(flush:true)) {
        log.info("Save image with id:"+newImage.id)
        data = newImage.encodeAsJSON()
        return [data : [success : true , message:"ok", image : newImage], status : 201]
      } else {
        log.error("Cannot save image:"+newImage.errors)
        return [data : [image : newImage , errors : [newImage.errors]], status : 400]
      }
  }

  def undo() {
    log.info("Undo")
    def imageData = JSON.parse(data)
    def image = Image.get(imageData.id)
    image.delete(flush:true)
    log.debug("Delete image with id:"+imageData.id)
    return [data : null, status : 200]
  }

  def redo() {

    log.info("Redo:"+data.replace("\n",""))
    def imageData = JSON.parse(data)
    def json = JSON.parse(postData)
    def image = Image.createImageFromData(json.image)
    image.id = imageData.id
    image.save(flush:true)
    log.debug("Save image:"+image.id)
    return [data : [image : image], status : 200]
  }
}
