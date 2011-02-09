package be.cytomine.command.annotation

import grails.converters.JSON
import be.cytomine.project.Annotation
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.project.Image
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand

class EditAnnotationCommand extends Command implements UndoRedoCommand  {


  def execute() {

    try
    {

      println "postData="+postData

      def postData = JSON.parse(postData)
      def updatedAnnotation = Annotation.get(postData.annotation.id)
      def backup = updatedAnnotation.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

      if (!updatedAnnotation ) {
        return [data : [success : false, message : "Annotation not found with id: " + postData.annotation.id], status : 404]
      }
      for (property in postData.annotation) {

        if(property.key.equals("location"))
        {
          //location is a Geometry object
          updatedAnnotation.properties.put(property.key, new WKTReader().read(property.value))
        }
        else if(property.key.equals("image"))
        {
          //image is a image object and not a simple id
          updatedAnnotation.properties.put(property.key, Image.get(property.value))
        }
        else if(!property.key.equals("class"))
        {
          //no property class
          updatedAnnotation.properties.put(property.key, property.value)
        }

      }

      if ( updatedAnnotation.validate()) {
        data = ([ previousAnnotation : (JSON.parse(backup)), newAnnotation :  updatedAnnotation]) as JSON
        updatedAnnotation.save()
        return [data : [success : true, message:"ok", annotation :  updatedAnnotation], status : 200]
      } else {
        return [data : [annotation :  updatedAnnotation, errors : [ updatedAnnotation.errors]], status : 400]
      }
    }catch(com.vividsolutions.jts.io.ParseException e)
    {
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).annotation.location +" is not valid:"+e.toString()]], status : 400]
    }


  }

  def undo() {
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.previousAnnotation.id)
    annotation.name = annotationsData.previousAnnotation.name
    annotation.location = new WKTReader().read(annotationsData.previousAnnotation.location)
    println  "undo="+annotation.location
    annotation.image = Image.get(annotationsData.previousAnnotation.image)
    annotation.save()
    return [data : [success : true, message:"ok", annotation : annotation], status : 200]
  }

  def redo() {
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.newAnnotation.id)
    annotation.name = annotationsData.newAnnotation.name
    annotation.location = new WKTReader().read(annotationsData.newAnnotation.location)
    annotation.image = Image.get(annotationsData.newAnnotation.image)
    annotation.save()
    return [data : [success : true, message:"ok", nnotation : annotation], status : 200]
  }
}
