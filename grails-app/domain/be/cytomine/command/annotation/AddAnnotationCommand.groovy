package be.cytomine.command.annotation

import be.cytomine.ontology.Annotation
import grails.converters.JSON

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import be.cytomine.image.ImageInstance
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import com.vividsolutions.jts.io.ParseException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException

class AddAnnotationCommand extends AddCommand implements UndoRedoCommand {

  boolean saveOnUndoRedoStack = true;

  def execute() throws CytomineException{
      try {
      json.user = user.id
      Annotation newAnnotation = Annotation.createFromData(json)

      if(!newAnnotation.location) throw new WrongArgumentException("Geo is null: 0 points")
      if(newAnnotation.location.getNumPoints()<1) throw new WrongArgumentException("Geo is empty:"+newAnnotation.location.getNumPoints() +" points")

      super.changeCurrentProject(newAnnotation?.image?.project)
      return super.validateAndSave(newAnnotation,["#ID#",newAnnotation?.imageFileName()] as Object[])
      } catch(com.vividsolutions.jts.io.ParseException ex) {
          throw new WrongArgumentException(ex.toString())
      }

  }

  def undo() {
    log.info("Undo")
    log.info("data="+data)
    def annotationData = JSON.parse(data)

    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    Annotation annotation = Annotation.get(annotationData.id)
    def callback = [annotationID : annotation.id , imageID : annotation.image.id ]
    annotation.delete(flush:true)
    String id = annotationData.id
    return super.createUndoMessage(id,annotation,[annotationData.id,filename] as Object[],callback);
  }

  def redo() {
    log.info("Redo")

    def annotationData = JSON.parse(data)
    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    def annotation = Annotation.createFromData(annotationData)
    def callback = [annotationID : annotationData.id , imageID : annotation.image.id ]
    annotation.id = annotationData.id
    annotation.save(flush:true)
    return super.createRedoMessage(annotation,[annotationData.id,filename] as Object[],callback);
  }


}
