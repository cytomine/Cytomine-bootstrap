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
    log.debug "undo data:"+ data
    def annotationData = JSON.parse(data)
    Annotation annotation = Annotation.createAnnotationFromData(annotationData)
    annotation.save()
    log.debug "annotation save with id " + annotation.id

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  annotation.id
    postData = postDataLocal.toString()

    return [data : [success : true, annotation : annotation, message : "OK"], status : 201]
  }

  def redo() {
    log.debug "redo data:"+ data
    def postData = JSON.parse(postData)
    Annotation annotation = Annotation.findById(postData.id)
    annotation.delete(flush:true);
    return [data : [success : true, message : "OK"], status : 204]

  }
}
