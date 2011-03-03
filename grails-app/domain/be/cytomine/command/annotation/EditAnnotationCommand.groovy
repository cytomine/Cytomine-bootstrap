package be.cytomine.command.annotation

import grails.converters.JSON
import be.cytomine.project.Annotation
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class EditAnnotationCommand extends Command implements UndoRedoCommand  {

  String toString() {"EditAnnotationCommand"}


  def execute() {

    try
    {
      log.info "Execute"
      log.debug "postData="+postData
      def postData = JSON.parse(postData)
      postData.annotation.user = user.id
      log.debug "Annotation id="+postData.annotation.id
      def updatedAnnotation = Annotation.get(postData.annotation.id)
      def backup = updatedAnnotation.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

      if (!updatedAnnotation ) {
        log.error "Annotation not found with id: " + postData.annotation.id
        return [data : [success : false, message : "Annotation not found with id: " + postData.annotation.id], status : 404]
      }

      updatedAnnotation = Annotation.getAnnotationFromData(updatedAnnotation,postData.annotation)
      updatedAnnotation.id = postData.annotation.id


      if ( updatedAnnotation.validate() && updatedAnnotation.save()) {
        log.info "New annotation is saved"
        def filename = updatedAnnotation.getImage().getFilename()
        def callback = [method : "be.cytomine.EditAnnotationCommand", annotationID : updatedAnnotation.id , imageID : updatedAnnotation.image.id ]
        def message = messageSource.getMessage('be.cytomine.EditAnnotationCommand', [updatedAnnotation.id, filename] as Object[], Locale.ENGLISH)
        data = ([ previousAnnotation : (JSON.parse(backup)), newAnnotation :  updatedAnnotation]) as JSON
        return [data : [success : true, annotation :  updatedAnnotation, message : message], status : 200]
      } else {
        log.error "New annotation can't be saved: " +  updatedAnnotation.errors
        return [data : [annotation :  updatedAnnotation, errors : updatedAnnotation.retrieveErrors()], status : 400]
      }
    }catch(com.vividsolutions.jts.io.ParseException e)
    {
      log.error "New annotation can't be saved (bad geom): " +  e.toString()
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).annotation.location +" is not valid:"+e.toString()]], status : 400]
    }


  }

  def undo() {
    log.info "Undo"
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.previousAnnotation.id)
    annotation = Annotation.getAnnotationFromData(annotation,annotationsData.previousAnnotation)
    annotation.save(flush:true)
    def filename = annotation.getImage().getFilename()
    def callback = [method : "be.cytomine.EditAnnotationCommand", annotationID : annotation.id , imageID : annotation.image.id ]
    //def callback =  "Cytomine.Views.Browser.updateAnnotation(" + annotation.id + "," + annotation.image.id + ")"
    def message = messageSource.getMessage('be.cytomine.EditAnnotationCommand', [annotation.id, filename] as Object[], Locale.ENGLISH)
    return [data : [success : true, message: message, callback : callback, annotation : annotation], status : 200]
  }

  def redo() {
    log.info "Redo"
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.newAnnotation.id)
    annotation = Annotation.getAnnotationFromData(annotation,annotationsData.newAnnotation)
    annotation.save(flush:true)
    def filename = annotation.getImage().getFilename()
    def callback = [method : "be.cytomine.EditAnnotationCommand", annotationID : annotation.id , imageID : annotation.image.id ]
    //def callback =  "Cytomine.Views.Browser.updateAnnotation(" + annotation.id + "," + annotation.image.id + ")"
    def message = messageSource.getMessage('be.cytomine.EditAnnotationCommand', [annotation.id, filename] as Object[], Locale.ENGLISH)
    return [data : [success : true, message: message, callback : callback, annotation : annotation], status : 200]
  }
}
