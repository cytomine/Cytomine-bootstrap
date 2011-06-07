package be.cytomine.command.abstractimage

import grails.converters.JSON

import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import be.cytomine.command.EditCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class EditAbstractImageCommand extends EditCommand implements UndoRedoCommand  {


  def execute() {
    log.info "Execute"
    log.debug "postData="+postData
    AbstractImage updatedImage=null
    try {
      def postData = JSON.parse(postData)
      updatedImage = AbstractImage.get(postData.id)
      return super.validateAndSave(postData,updatedImage,[updatedImage.id,updatedImage.filename] as Object[])

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
    AbstractImage image = AbstractImage.findById(imageData.previousImage.id)
    image = AbstractImage.getFromData(image,imageData.previousImage)
    image.save(flush:true)
    super.createUndoMessage(imageData, image, [image.id,image.filename] as Object[])
  }

  def redo() {
    log.info "Redo"
    def imageData = JSON.parse(data)
    AbstractImage image = AbstractImage.findById(imageData.newImage.id)
    image = AbstractImage.getFromData(image,imageData.newImage)
    image.save(flush:true)
    super.createRedoMessage(imageData, image, [image.id,image.filename] as Object[])
  }

}
