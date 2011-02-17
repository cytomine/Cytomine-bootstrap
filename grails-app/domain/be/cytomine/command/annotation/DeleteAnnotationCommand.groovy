package be.cytomine.command.annotation

import be.cytomine.security.User
import be.cytomine.project.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class DeleteAnnotationCommand extends Command implements UndoRedoCommand{

  def execute() {

    log.info "Execute"
    def postData = JSON.parse(postData)

    Annotation annotation = Annotation.findById(postData.id)
    data = annotation.encodeAsJSON()

    if (!annotation) {
      log.error "Annotation not found with id: " + postData.id
      return [data : [success : false, message : "Annotation not found with id: " + postData.id], status : 404]
    }
    log.info "Delete annotation " + postData.id
    annotation.delete();
    return [data : [success : true, message : "OK", data : [annotation : postData.id]], status : 204]
  }

  def undo() {
    log.info "Undo"
    def annotationData = JSON.parse(data)
    Annotation annotation = new Annotation(annotationData)
    annotationData.save(flush:true)
    return [data : [success : true, annotation : annotation, message : "OK"], status : 201]
  }

  def redo() {
    log.info "Redo"
    def postData = JSON.parse(postData)
    Annotation annotation = User.findById(postData.id)
    annotation.delete(flush:true);
    return [data : [success : true, message : "OK"], status : 204]

  }
}
