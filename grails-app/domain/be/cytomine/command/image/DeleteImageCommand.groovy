package be.cytomine.command.image

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.Image
import be.cytomine.command.DeleteCommand

class DeleteImageCommand extends DeleteCommand implements UndoRedoCommand{

  def execute() {

    log.info "Execute"
    def postData = JSON.parse(postData)

    Image image = Image.findById(postData.id)
    data = image.encodeAsJSON()

    if (!image) {
      log.error "Image not found with id: " + postData.id
      return [data : [success : false, message : "Image not found with id: " + postData.id], status : 404]
    }
    log.info "Delete image " + postData.id
    try {
      image.delete(flush:true);
      return [data : [success : true, message : "OK", data : [image : postData.id]], status : 200]
    } catch(org.springframework.dao.DataIntegrityViolationException e)
    {
      log.error(e)
      return [data : [success : false, errors : "Image has still data (annotation,...)"], status : 400]
    }
  }

  def undo() {
    log.info "Undo"
    def imageData = JSON.parse(data)
    Image image = Image.createImageFromData(imageData)
    image.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  image.id
    postData = postDataLocal.toString()

    log.debug "image save with id " + image.id
    return [data : [success : true, image : image, message : "OK"], status : 201]
  }

  def redo() {
    log.info "Redo"
    def postData = JSON.parse(postData)
    Image image = Image.findById(postData.id)
    image.delete(flush:true);
    return [data : [success : true, message : "OK"], status : 200]

  }

}
