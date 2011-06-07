package be.cytomine.command.annotation

import grails.converters.JSON
import be.cytomine.ontology.Annotation

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.EditCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class EditAnnotationCommand extends EditCommand implements UndoRedoCommand  {

  boolean saveOnUndoRedoStack = true;

  def execute() {
    log.info "Execute"
    log.debug "postData="+postData
    Annotation updatedAnnotation=null
    try {
      def postData = JSON.parse(postData)
      postData.user = user.id
      updatedAnnotation = Annotation.get(postData.id)
      String filename = updatedAnnotation.image?.baseImage?.getFilename()
      return super.validateAndSave(postData,updatedAnnotation,[updatedAnnotation.id,filename] as Object[])

    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(ConstraintException e) {
      log.error(e)
      return [data : [success : false, errors : updatedAnnotation.retrieveErrors()], status : 400]
    } catch(IllegalArgumentException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    } catch(com.vividsolutions.jts.io.ParseException e)
    {
      log.error "New annotation can't be saved (bad geom): " +  e.toString()
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).location +" is not valid:"+e.toString()]], status : 400]
    }

  }

  def undo() {
    log.info "Undo"
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.previousAnnotation.id)
    annotation = Annotation.getFromData(annotation,annotationsData.previousAnnotation)
    annotation.save(flush:true)
    def filename = annotation.image?.baseImage?.getFilename()
    def callback = [annotationID : annotation.id , imageID : annotation.image.id ]
    super.createUndoMessage(annotationsData, annotation, [annotation.id,filename] as Object[],callback)
  }


  def redo() {
    log.info "Redo"
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.newAnnotation.id)
    annotation = Annotation.getFromData(annotation,annotationsData.newAnnotation)
    annotation.save(flush:true)
    def filename = annotation.image?.baseImage?.getFilename()
    def callback = [annotationID : annotation.id , imageID : annotation.image.id ]
     super.createRedoMessage(annotationsData, annotation, [annotation.id,filename] as Object[],callback)
  }

}
