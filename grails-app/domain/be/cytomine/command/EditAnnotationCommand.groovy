package be.cytomine.command

import grails.converters.JSON
import be.cytomine.project.Annotation

class EditAnnotationCommand extends Command implements UndoRedoCommand  {


  def execute() {
    def postData = JSON.parse(postData)
    def updatedAnnotation = Annotation.get(postData.annotation.id)
    def backup = updatedAnnotation.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

    if (!updatedAnnotation ) {
      return [data : [success : false, message : "Annotation not found with id: " + postData.annotation.id], status : 404]
    }

    for (property in postData.annotation) {

      updatedAnnotation.properties.put(property.key, property.value)
    }


    if ( updatedAnnotation.validate()) {
      data = ([ previousUser : (JSON.parse(backup)), newUser :  updatedAnnotation]) as JSON
      updatedAnnotation.save()
      return [data : [success : true, message:"ok", user :  updatedAnnotation], status : 200]
    } else {
      return [data : [user :  updatedAnnotation, errors : [ updatedAnnotation.errors]], status : 403]
    }


  }

  def undo() {
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.previousAnnotation.id)
    annotation.name = annotationsData.previousAnnotation.name
    annotation.location = annotationsData.previousAnnotation.location
    annotation.scan = annotationsData.previousAnnotation.scan
    annotation.save()
    return [data : [success : true, message:"ok", annotation : annotation], status : 200]
  }

  def redo() {
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.newAnnotation.id)
    annotation.name = annotationsData.newUser.name
    annotation.location = annotationsData.newUser.location
    annotation.scan = annotationsData.newUser.scan
    annotation.save()
    return [data : [success : true, message:"ok", nnotation : annotation], status : 200]
  }
}
