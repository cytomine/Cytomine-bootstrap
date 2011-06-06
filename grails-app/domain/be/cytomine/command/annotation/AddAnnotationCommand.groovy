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
      return super.validateAndSave(newAnnotation,"Annotation",["#ID#",newAnnotation?.imageFileName()] as Object[])

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
    log.info("data="+data)
    def annotationData = JSON.parse(data)
    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("imageID",annotationData.image)
    Annotation annotation = Annotation.get(annotationData.id)
    annotation.delete(flush:true)

    log.info("annotationData.id="+annotationData.id + " filename="+filename)
    String id = annotationData.id
    return super.createUndoMessage(
            id,
            'Annotation',
            [annotationData.id,filename] as Object[],
            callback
    );
  }

  def redo() {
    log.info("Redo data:"+data.replace("\n",""))
    log.info("Redo postData:"+postData.replace("\n",""))
    def annotationData = JSON.parse(data)
    def json = JSON.parse(postData)
    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("imageID",annotationData.image)

    def annotation = Annotation.createFromData(annotationData)
    annotation.id = annotationData.id
    log.info "validate=" + annotation.validate()
    log.info "errors=" + annotation.errors
    log.info "save=" + annotation.save(flush:true)

    return super.createRedoMessage(
            annotation,
            'Annotation',
            [annotationData.id,filename] as Object[],
            callback
    );
  }

}
