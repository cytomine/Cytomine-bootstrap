package be.cytomine.command.imageinstance

import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand

import grails.converters.JSON
import be.cytomine.image.ImageInstance
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class EditImageInstanceCommand extends EditCommand implements UndoRedoCommand  {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    log.debug "postData="+postData
    ImageInstance updatedImage=null
    try {
      def postData = JSON.parse(postData)
      updatedImage = ImageInstance.get(postData.id)
      return super.validateAndSave(postData,updatedImage,[updatedImage.id, updatedImage?.baseImage?.filename,updatedImage.project.name] as Object[])

    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(ConstraintException e) {
      log.error(e)
      return [data : [success : false, errors : updatedImage.retrieveErrors()], status : 400]
    } catch(IllegalArgumentException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    }

  }

  def undo() {
    log.info "Undo"
    def imageData = JSON.parse(data)
    ImageInstance image = ImageInstance.findById(imageData.previousImageInstance.id)
    image = ImageInstance.getFromData(image,imageData.previousImageInstance)
    image.save(flush:true)
    super.createUndoMessage(imageData, image, [image.id, image?.baseImage?.filename,image.project.name] as Object[])
  }

  def redo() {
    log.info "Redo"
    def imageData = JSON.parse(data)
    ImageInstance image = ImageInstance.findById(imageData.newImageInstance.id)
    image = ImageInstance.getFromData(image,imageData.newImageInstance)
    image.save(flush:true)
    super.createRedoMessage(imageData, image, [image.id, image?.baseImage?.filename,image.project.name] as Object[])
  }
}
