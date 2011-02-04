package be.cytomine.command.annotation

import be.cytomine.security.User
import be.cytomine.project.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class AddAnnotationCommand extends Command implements UndoRedoCommand {

  def execute() {
    def newAnnotation = Annotation.getAnnotationFromData(JSON.parse(postData))
    if(newAnnotation.validate()) {
      newAnnotation.save()
      //data = newAnnotation.encodeAsJSON()
      data = Annotation.convertToMap(newAnnotation)
      return [data : [success : true , message:"ok", annotation : newAnnotation], status : 201]
    } else {
      return [data : [user : newAnnotation , errors : [newAnnotation.errors]], status : 403]

    }
  }

  def undo() {
    def annotationData = JSON.parse(data)
    def annotation = Annotation.findById(annotationData.id)
    annotation.delete()
    return [data : null, status : 200]
  }

  def redo() {


    println "data = " + data

    def annotationData = JSON.parse(data)

    def annotation = Annotation.getAnnotationFromData(JSON.parse(postData))
    annotation.id = annotationData.id
    annotation.save()
    return [data : [annotation : annotation], status : 200]
  }
}
