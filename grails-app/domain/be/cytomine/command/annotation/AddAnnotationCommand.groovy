package be.cytomine.command.annotation

import be.cytomine.ontology.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand

class AddAnnotationCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  String toString() {"ADD ANNOTATION"}

  /*AddAnnotationCommand() {
    log.info("Default constructor")
    objectType = "ANNOTATION"
  } */

  def execute() {
    try
    {
      log.info("Execute")
      def json = JSON.parse(postData)
      json.user = user.id
      Annotation annotation = Annotation.createAnnotationFromData(json)



      if(annotation.validate() && annotation.save(flush : true)) {
        log.info("Save annotation with id:"+annotation.id)
        data = annotation.encodeAsJSON()
        def filename = annotation.image?.baseImage?.getFilename()
        def message = messageSource.getMessage('be.cytomine.AddAnnotationCommand', [annotation.id, filename] as Object[], Locale.ENGLISH)

        actionMessage = "ADD ANNOTATION " + annotation

        return [data : [success : true , message: message, annotation : annotation], status : 201]
      } else {
        log.error("Cannot save annotation:"+annotation.errors)
        return [data : [annotation : annotation , errors : annotation.retrieveErrors()], status : 400]
      }
    }catch(com.vividsolutions.jts.io.ParseException e)
    {
      log.error("Cannot save annotation with bad geometry:"+e.toString())
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).location +" is not valid:"+e.toString()]], status : 400]
    }catch(Exception e)
    {
      log.error("Cannot save annotation"+e.toString())
      e.printStackTrace()
      return [data : [annotation : null , errors : ["Annotation is not valid:"+e.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def annotationData = JSON.parse(data)

    def annotation = Annotation.get(annotationData.id)
    def filename = annotation.image?.baseImage?.getFilename()
    annotation.delete(flush:true)
    //def callback =  "Cytomine.Views.Browser.removeAnnotation(" + annotationData.id + "," + annotation.image.id + ")"
    def callback = [method : "be.cytomine.DeleteAnnotationCommand", annotationID : annotationData.id , imageID : annotation.image.id ]
    def message = messageSource.getMessage('be.cytomine.DeleteAnnotationCommand', [annotationData.id, filename] as Object[], Locale.ENGLISH)
    log.debug("Delete annotation with id:"+annotationData.id)
    return [data : [message : message, annotation : annotationData.id, callback : callback], status : 200]
  }

  def redo() {

    log.info("Redo:"+data.replace("\n",""))
    def annotationData = JSON.parse(data)
    def json = JSON.parse(postData)
    json.user = user.id
    Annotation annotation = Annotation.createAnnotationFromData(json)
    def filename = annotation.image?.baseImage?.getFilename()
    annotation.id = annotationData.id
    annotation.save(flush:true)
    log.debug("Save annotation:"+annotation.id)
    def callback = [method : "be.cytomine.AddAnnotationCommand", annotationID : annotationData.id , imageID : annotation.image.id ]
    def message = messageSource.getMessage('be.cytomine.AddAnnotationCommand', [annotationData.id, filename] as Object[], Locale.ENGLISH)
    log.debug("Delete annotation with id:"+annotationData.id)

    return [data : [annotation : annotation, message : message, callback : callback], status : 201]
  }
}
