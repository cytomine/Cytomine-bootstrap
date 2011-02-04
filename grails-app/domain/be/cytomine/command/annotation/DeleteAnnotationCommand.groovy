package be.cytomine.command.annotation

import be.cytomine.security.User
import be.cytomine.project.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class DeleteAnnotationCommand extends Command implements UndoRedoCommand{

  def execute() {
    def postData = JSON.parse(postData)

    Annotation annotation = Annotation.findById(postData.id)
    data = annotation.encodeAsJSON()

    if (!annotation) {
      return [data : [success : false, message : "Annotation not found with id: " + postData.id], status : 404]
    }

    annotation.delete();
    return [data : [success : true, message : "OK", data : [annotation : postData.id]], status : 204]
  }

  def undo() {
    def annotationData = JSON.parse(data)
    Annotation annotation = new Annotation(annotationData)
    annotationData.save()
    return [data : [success : true, annotation : annotation, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    Annotation annotation = User.findById(postData.id)
    annotation.delete();
    return [data : [success : true, message : "OK"], status : 204]

  }
}
