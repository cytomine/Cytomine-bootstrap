package be.cytomine.command.annotationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand

class AddAnnotationTermCommand extends AddCommand implements UndoRedoCommand {

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
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(Annotation.get(annotationTermData.annotation),Term.get(annotationTermData.term))
    AnnotationTerm.unlink(annotationTerm.annotation,annotationTerm.term)
    log.debug("Delete annotationTerm with id:"+annotationTermData.id)
    return [data : ["AnnotationTerm deleted"], status : 200]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def annotationTermData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def annotationTerm = AnnotationTerm.createAnnotationTermFromData(json)
    annotationTerm = AnnotationTerm.link(annotationTermData.id,annotationTerm.annotation,annotationTerm.term)
    //println "annotationTermData.id="+annotationTermData.id

    log.debug("Save annotationTerm:"+annotationTerm.id)
    /*def session = sessionFactory.getCurrentSession()
    session.clear()     */
    //hibSession.

    return [data : [annotationTerm : annotationTerm], status : 201]
  }
  //def sessionFactory

}
