package be.cytomine.command.image

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
import be.cytomine.security.User
import be.cytomine.project.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Image

class DeleteImageCommand extends Command implements UndoRedoCommand{

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
    image.delete();
    return [data : [success : true, message : "OK", data : [image : postData.id]], status : 204]
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
    return [data : [success : true, message : "OK"], status : 204]

  }

}
