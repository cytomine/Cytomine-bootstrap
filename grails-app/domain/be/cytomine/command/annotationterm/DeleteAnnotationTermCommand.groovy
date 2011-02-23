package be.cytomine.command.annotationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.AnnotationTerm

class DeleteAnnotationTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    AnnotationTerm annotationTerm = AnnotationTerm.findById(postData.id)
    data = annotationTerm.encodeAsJSON()

    if (!annotationTerm) {
      return [data : [success : false, message : "AnnotationTerm not found with id: " + postData.id], status : 404]
    }
    AnnotationTerm.unlink(annotationTerm.annotation, annotationTerm.term)
    //annotationTerm.delete();
    return [data : [success : true, message : "OK", data : [annotationTerm : postData.id]], status : 204]
  }

  def undo() {
    def annotationTermData = JSON.parse(data)
    AnnotationTerm annotationTerm = AnnotationTerm.createAnnotationTermFromData(annotationTermData)
    annotationTerm.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  annotationTerm.id
    postData = postDataLocal.toString()
    AnnotationTerm.link(annotationTerm.id,annotationTerm.annotation, annotationTerm.term)
    log.debug "AnnotationTerm with id " + annotationTerm.id

    return [data : [success : true, annotationTerm : annotationTerm, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    AnnotationTerm annotationTerm = AnnotationTerm.findById(postData.id)
    AnnotationTerm.unlink(annotationTerm.annotation, annotationTerm.term)
    return [data : [success : true, message : "OK"], status : 204]

  }

}