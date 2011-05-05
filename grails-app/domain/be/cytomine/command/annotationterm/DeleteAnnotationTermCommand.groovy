package be.cytomine.command.annotationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.command.DeleteCommand

class DeleteAnnotationTermCommand extends DeleteCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
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
    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)

    AnnotationTerm annotationTerm = AnnotationTerm.createAnnotationTermFromData(annotationTermData)

    annotationTerm = AnnotationTerm.link(annotationTermData.id,annotation, term)


    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  annotationTerm.id
    postData = postDataLocal.toString()

    log.debug "AnnotationTerm with id " + annotationTerm.id

    def callback = [method : "be.cytomine.AddAnnotationTermCommand", annotationID : annotation.id , termID : term.id, imageID:annotation.image.id  ]
    def message = messageSource.getMessage('be.cytomine.AddAnnotationTermCommand', [annotation.name,term.name] as Object[], Locale.ENGLISH)
    log.debug("Add annotationTerm with id:"+annotationTermData.id)

    return [data : [annotationTerm : annotationTerm, message : message, callback : callback], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)

      Annotation annotation = Annotation.get(postData.annotation)
      Term term = Term.get(postData.term)
    AnnotationTerm annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation,term)
    AnnotationTerm.unlink(annotationTerm.annotation, annotationTerm.term)

      def callback = [method : "be.cytomine.DeleteAnnotationTermCommand", annotationID : annotation.id , termID : term.id , imageID:annotation.image.id]
      def message = messageSource.getMessage('be.cytomine.DeleteAnnotationTermCommand', [annotation.name,term.name] as Object[], Locale.ENGLISH)
      //return [data : ["AnnotationTerm deleted"], status : 200]
      return [data : [message : message, annotationTerm : annotationTerm.id, callback : callback], status : 200]

  }

}