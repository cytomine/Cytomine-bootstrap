package be.cytomine.command.annotation

import be.cytomine.ontology.Annotation
import grails.converters.JSON

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import be.cytomine.image.ImageInstance
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import com.vividsolutions.jts.io.ParseException

class AddAnnotationCommand extends AddCommand implements UndoRedoCommand {

  boolean saveOnUndoRedoStack = true;

  def execute() {
    log.info("Execute")
    Annotation newAnnotation=null
    try {
      def json = JSON.parse(postData)
      json.user = user.id
      newAnnotation = Annotation.createFromData(json)
      if(newAnnotation.location == null || newAnnotation.location.getNumPoints()<1) throw new ParseException("Gemo is empty:"+newAnnotation.location.getNumPoints() +" points")
      log.info "newAnnotation.project=" +newAnnotation?.image?.project
      super.changeCurrentProject(newAnnotation?.image?.project)
      return super.validateAndSave(newAnnotation,["#ID#",newAnnotation?.imageFileName()] as Object[])

    }catch(ConstraintException  ex){
      return [data : [annotation:newAnnotation,errors:newAnnotation.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [annotation:null,errors:["Cannot save object:"+ex.toString()]], status : 400]
    }catch(ParseException e) {
      log.error("Cannot save annotation with bad geometry:"+e.toString())
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).location +" is not valid:"+e.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    log.info("data="+data)
    def annotationData = JSON.parse(data)

    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    /*HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("imageID",annotationData.image)*/
    Annotation annotation = Annotation.get(annotationData.id)
    def callback = [annotationID : annotation.id , imageID : annotation.image.id ]
    annotation.delete(flush:true)

    log.info("annotationData.id="+annotationData.id + " filename="+filename)

    String id = annotationData.id
    return super.createUndoMessage(id,annotation,[annotationData.id,filename] as Object[],callback);
  }

  def redo() {
    log.info("Redo data:"+data.replace("\n",""))
    log.info("Redo postData:"+postData.replace("\n",""))

    def annotationData = JSON.parse(data)
    String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
    /*HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("imageID",annotationData.image) */
    def annotation = Annotation.createFromData(annotationData)
    def callback = [annotationID : annotationData.id , imageID : annotation.image.id ]
    annotation.id = annotationData.id
    annotation.save(flush:true)
    return super.createRedoMessage(annotation,[annotationData.id,filename] as Object[],callback);
  }


}
