package be.cytomine.command.annotation

import be.cytomine.ontology.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.DeleteCommand

class DeleteAnnotationCommand extends DeleteCommand implements UndoRedoCommand{

  boolean saveOnUndoRedoStack = true;

  String toString() {"DeleteAnnotationCommand"}

  def execute() {

    log.info "Execute"
    def postData = JSON.parse(postData)

    Annotation annotation = Annotation.findById(postData.id)

    actionMessage = "DELETE ANNOTATION " + annotation

    data = annotation.encodeAsJSON()

    if (!annotation) {
      log.error "Annotation not found with id: " + postData.id
      return [data : [success : false, message : "Annotation not found with id: " + postData.id], status : 404]
    }
    log.info "Delete annotation " + postData.id
    try {
      annotation.delete(flush:true);
      def filename = annotation.getImage().getFilename()
      def message = messageSource.getMessage('be.cytomine.DeleteAnnotationCommand', [annotation.id, filename] as Object[], Locale.ENGLISH)
      return [data : [success : true, message : message, data : [annotation : postData.id]], status : 200]
    } catch(org.springframework.dao.DataIntegrityViolationException e)
    {
      log.error(e)
      return [data : [success : false, errors : "Annotation is still map with data (term,...)"], status : 400]
    }
  }

  def undo() {
    log.debug "undo data:"+ data
    def annotationData = JSON.parse(data)
    Annotation annotation = Annotation.createAnnotationFromData(annotationData)
    annotation.id = annotationData.id;
    annotation.save()
    log.debug "annotation save with id " + annotation.id
    def filename = annotation.getImage().getFilename()
    def callback = [method : "be.cytomine.AddAnnotationCommand", annotationID : annotation.id , imageID : annotation.image.id ]
    def message = messageSource.getMessage('be.cytomine.AddAnnotationCommand', [annotation.id, filename] as Object[], Locale.ENGLISH)
    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  annotation.id
    postData = postDataLocal.toString()

    return [data : [success : true, annotation : annotation, message : message, callback : callback], status : 201]
  }

  def redo() {
    log.debug "redo data:"+ data
    def postData = JSON.parse(postData)
    Annotation annotation = Annotation.findById(postData.id)
    annotation.delete(flush:true);
    def filename = annotation.getImage().getFilename()
    def callback = [method : "be.cytomine.DeleteAnnotationCommand", annotationID : annotation.id , imageID : annotation.image.id ]
    def message = messageSource.getMessage('be.cytomine.DeleteAnnotationCommand', [annotation.id, filename] as Object[], Locale.ENGLISH)
    return [data : [success : true, message : message, callback: callback], status : 200]

  }
}
