package be.cytomine.command.annotationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.command.DeleteCommand

class DeleteAnnotationTermCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)


      Annotation annotation = Annotation.get(postData.annotation)
      Term term = Term.get(postData.term)

    log.info "execute with annotation=" + annotation + " term=" + term
    AnnotationTerm annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation,term)
    data = annotationTerm.encodeAsJSON()

    if (!annotationTerm) {
      return [data : [success : false, message : "AnnotationTerm not found with id: " + postData.id], status : 404]
    }
    log.info "Unlink=" + annotationTerm.annotation +" " + annotationTerm.term
    AnnotationTerm.unlink(annotationTerm.annotation, annotationTerm.term)

    return [data : [success : true, message : "OK", data : [annotationTerm : postData.id]], status : 200]
  }

  def undo() {


    def annotationTermData = JSON.parse(data)
    AnnotationTerm annotationTerm = AnnotationTerm.createAnnotationTermFromData(annotationTermData)
    annotationTerm = AnnotationTerm.link(annotationTermData.id,annotationTerm.annotation, annotationTerm.term)
    //annotationTerm.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  annotationTerm.id
    postData = postDataLocal.toString()

    log.debug "AnnotationTerm with id " + annotationTerm.id

    return [data : [success : true, annotationTerm : annotationTerm, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)

      Annotation annotation = Annotation.get(postData.annotation)
      Term term = Term.get(postData.term)
    AnnotationTerm annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation,term)
    AnnotationTerm.unlink(annotationTerm.annotation, annotationTerm.term)
    return [data : [success : true, message : "OK"], status : 200]

  }

}