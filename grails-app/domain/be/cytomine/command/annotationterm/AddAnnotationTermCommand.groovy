package be.cytomine.command.annotationterm

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand
import be.cytomine.ontology.RelationTerm
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddAnnotationTermCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {   //must be refactored with AddCommand
    log.info("Execute")
    AnnotationTerm newAnnotationTerm
    try {
      def json = JSON.parse(postData)
      newAnnotationTerm = AnnotationTerm.createAnnotationTermFromData(json)
      AnnotationTerm.link(newAnnotationTerm.annotation,newAnnotationTerm.term)
        return super.validateWithoutSave(
                newAnnotationTerm,
                "AnnotationTerm",
                ["#ID#",newAnnotationTerm.annotation.id,newAnnotationTerm.term.name] as Object[])

      }catch(ConstraintException  ex){
      return [data : [annotationterm:newAnnotationTerm,errors:newAnnotationTerm.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [annotationterm:null,errors:["Cannot save annotation-term:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    log.info("data="+data)
    def annotationTermData = JSON.parse(data)
    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation,term)
    AnnotationTerm.unlink(annotationTerm.annotation,annotationTerm.term)
    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("annotationID",annotation.id)
    callback.put("termID",term.id)
    callback.put("imageID",annotation.image.id)

    log.debug "AnnotationTerm=" + annotationTermData.id +" annotation.name=" + annotation.name  + " term.name=" + term.name
    String id = annotationTermData.id
    return super.createUndoMessage(
            id,
            'AnnotationTerm',
            [annotation.name,term.name] as Object[],
            callback);
  }



  def redo() {
    log.info("Redo")
    def annotationTermData = JSON.parse(data)
    def json = JSON.parse(postData)

    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)

    def annotationTerm = AnnotationTerm.createAnnotationTermFromData(json)

    AnnotationTerm.link(annotation,term)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("annotationID",annotation.id)
    callback.put("termID",term.id)
    callback.put("imageID",annotation.image.id)

    return super.createRedoMessage(
            annotationTerm,
            'AnnotationTerm',
            [annotation.name,term.name] as Object[],
            callback);
  }

}
