package be.cytomine.command.annotationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand

class AddAnnotationTermCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      AnnotationTerm newAnnotationTerm = AnnotationTerm.createAnnotationTermFromData(json)
      if (newAnnotationTerm.validate()) {
        newAnnotationTerm =  AnnotationTerm.link(newAnnotationTerm.annotation,newAnnotationTerm.term)
        log.info("Save AnnotationTerm with id:"+newAnnotationTerm.id)
        data = newAnnotationTerm.encodeAsJSON()
        return [data : [success : true, message:"ok", annotationTerm : newAnnotationTerm], status : 201]
      } else {
        return [data : [annotationTerm : newAnnotationTerm, errors : newAnnotationTerm.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save annotationTerm:"+ex.toString())
      return [data : [annotationTerm : null , errors : ["Cannot save annotationTerm:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def annotationTermData = JSON.parse(data)
    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation,term)
    AnnotationTerm.unlink(annotationTerm.annotation,annotationTerm.term)
    log.debug("Delete annotationTerm with id:"+annotationTermData.id)

    def callback = [method : "be.cytomine.DeleteAnnotationTermCommand", annotationID : annotation.id , termID : term.id , imageID:annotation.image.id]
    def message = messageSource.getMessage('be.cytomine.DeleteAnnotationTermCommand', [annotation.name,term.name] as Object[], Locale.ENGLISH)
    //return [data : ["AnnotationTerm deleted"], status : 200]
    return [data : [message : message, annotationTerm : annotationTerm.id, callback : callback], status : 200]
   // [message : message, annotation : annotationData.id, callback : callback] <<<<=======
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def annotationTermData = JSON.parse(data)
    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)

    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def annotationTerm = AnnotationTerm.createAnnotationTermFromData(json)
    annotationTerm = AnnotationTerm.link(annotationTermData.id,annotationTerm.annotation,annotationTerm.term)
    //println "annotationTermData.id="+annotationTermData.id

    log.debug("Save annotationTerm:"+annotationTerm.id)
    /*def session = sessionFactory.getCurrentSession()
    session.clear()     */
    //hibSession.
    def callback = [method : "be.cytomine.AddAnnotationTermCommand", annotationID : annotation.id , termID : term.id, imageID:annotation.image.id  ]
    def message = messageSource.getMessage('be.cytomine.AddAnnotationTermCommand', [annotation.name,term.name] as Object[], Locale.ENGLISH)
    log.debug("Add annotationTerm with id:"+annotationTermData.id)

    return [data : [annotationTerm : annotationTerm, message : message, callback : callback], status : 201]
  }
  //def sessionFactory

}
