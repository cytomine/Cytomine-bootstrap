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
      log.info("Execute")
      Annotation newAnnotation = Annotation.getAnnotationFromData(JSON.parse(postData))
      if(newAnnotation.validate() && newAnnotation.save(flush:true)) {
        log.info("Save annotation with id:"+newAnnotation.id)
        data = newAnnotation.encodeAsJSON()
        return [data : [success : true , message:"ok", annotation : newAnnotation], status : 201]
      } else {
        log.error("Cannot save annotation:"+newAnnotation.errors)
        return [data : [annotation : newAnnotation , errors : [newAnnotation.errors]], status : 400]
      }
    }catch(com.vividsolutions.jts.io.ParseException e)
    {
      log.error("Cannot save annotation with bad geometry:"+e.toString())
      return [data : [annotation : null , errors : ["Geometry "+ JSON.parse(postData).annotation.location +" is not valid:"+e.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def annotationData = JSON.parse(data)

    def annotation = Annotation.get(annotationData.id)
    annotation.delete(flush:true)
    log.debug("Delete annotation with id:"+annotationData.id)
    return [data : null, status : 200]
  }

  def redo() {

    log.info("Redo:"+data.replace("\n",""))
    def annotationData = JSON.parse(data)
    def annotation = Annotation.getAnnotationFromData(JSON.parse(postData))
    annotation.id = annotationData.id
    annotation.save(flush:true)
    log.debug("Save annotation:"+annotation.id)
    return [data : [annotation : annotation], status : 200]
  }
}
