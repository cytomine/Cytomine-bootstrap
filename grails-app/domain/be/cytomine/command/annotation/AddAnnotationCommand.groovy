package be.cytomine.command.annotation

import be.cytomine.security.User
import be.cytomine.project.Annotation
import grails.converters.JSON
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class AddAnnotationCommand extends Command implements UndoRedoCommand {

  def execute() {
    try
    {
      Annotation newAnnotation = Annotation.getAnnotationFromData(JSON.parse(postData))
      if(newAnnotation.validate()) {
        newAnnotation.save()
        println "save annotation with id:"+newAnnotation.id
        data = newAnnotation.encodeAsJSON()
        return [data : [success : true , message:"ok", annotation : newAnnotation], status : 201]
      } else {
        return [data : [annotation : newAnnotation , errors : [newAnnotation.errors]], status : 400]

      }
    }catch(com.vividsolutions.jts.io.ParseException e)
    {
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).annotation.location +" is not valid:"+e.toString()]], status : 400]
    }
  }

  def undo() {
    println "undo"
    def annotationData = JSON.parse(data)
    println "id="+ annotationData.id
    def annotation = Annotation.get(annotationData.id)
    println "delete"
    annotation.delete()
    println "return"
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
