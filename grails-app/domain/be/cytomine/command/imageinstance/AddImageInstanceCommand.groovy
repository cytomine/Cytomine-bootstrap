package be.cytomine.command.imageinstance

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
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
class AddImageInstanceCommand extends AddCommand implements UndoRedoCommand {

  def execute() {
    ImageInstance newImage
    try{
      log.info("Execute")
      def json = JSON.parse(postData)
      json.user = user.id
      newImage = ImageInstance.createImageInstanceFromData(json)
      return super.validateAndSave(newImage,"ImageInstance",["#ID#",json.name,newImage.project.name] as Object[])
    }catch(ConstraintException ex){
      return [data : [imageinstance:newImage,errors:newImage.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [imageinstance:null,errors:["Cannot save imageinstance:"+ex.toString()]], status : 400]
    }
  }


  def undo() {
    log.info("Undo")
    def imageData = JSON.parse(data)
    ImageInstance image = ImageInstance.get(imageData.id)
    image.delete(flush:true)

    String id = imageData.id

    return super.createUndoMessage(
            id,
            'ImageInstance',
            [imageData.id,AbstractImage.read(imageData.baseImage).filename] as Object[]
    );
  }


  def redo() {

    log.info("Redo:"+data.replace("\n",""))
    def imageData = JSON.parse(data)
    def json = JSON.parse(postData)
    ImageInstance image = ImageInstance.createImageInstanceFromData(imageData)
    image.id = imageData.id
    image.save(flush:true)
    log.debug("Save image:"+image.id)
    return super.createRedoMessage(
            image,
            'ImageInstance',
            [imageData.id,imageData.name] as Object[]
    );
  }



}
