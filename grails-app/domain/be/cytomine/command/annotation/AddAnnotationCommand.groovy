package be.cytomine.command.annotation

import be.cytomine.ontology.Annotation
import grails.converters.JSON

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import be.cytomine.image.ImageInstance
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddAnnotationCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  String toString() {"ADD ANNOTATION"}

  def execute() {
    log.info("Execute")
    Annotation newAnnotation
    try {
      def json = JSON.parse(postData)
      json.user = user.id
      newAnnotation = Annotation.createFromData(json)
      String filename = newAnnotation?.image?.baseImage?.filename
      return super.validateAndSave(newAnnotation,"Annotation",["#ID#",filename] as Object[])

    }catch(ConstraintException  ex){
      return [data : [annotation:newAnnotation,errors:newAnnotation.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [annotation:null,errors:["Cannot save object:"+ex.toString()]], status : 400]
    }catch(com.vividsolutions.jts.io.ParseException e) {
      log.error("Cannot save annotation with bad geometry:"+e.toString())
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).location +" is not valid:"+e.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def annotationData = JSON.parse(data)
    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("imageID",annotationData.image)
    return super.undo(annotationData,new Annotation(),'Annotation',[annotationData.id,filename] as Object[],callback);
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def annotationData = JSON.parse(data)
    def json = JSON.parse(postData)
    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("imageID",annotationData.image)
    return super.redo(annotationData,json,new Annotation(),'Annotation',[annotationData.id,filename] as Object[],callback);
  }

}
