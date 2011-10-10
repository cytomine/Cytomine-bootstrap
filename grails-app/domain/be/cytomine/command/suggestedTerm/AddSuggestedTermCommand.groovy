package be.cytomine.command.suggestedTerm

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Annotation
import be.cytomine.processing.Job

class AddSuggestedTermCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info("Execute")
    SuggestedTerm newSuggestedTerm
    try {
      def json = JSON.parse(postData)
        log.info("json"+json)
        log.info("json.annotation="+json.annotation)
      newSuggestedTerm = SuggestedTerm.createFromData(json)
      return super.validateAndSave(newSuggestedTerm,[
              "#ID#",
              Term.read(json.term)?.name,
              Annotation.read(json.annotation)?.id,
              Job.read(json.job)?.software?.name] as Object[]
      )
      //errors:
    }catch(ConstraintException  ex){
      return [data : [suggestedTerm:newSuggestedTerm,errors:newSuggestedTerm.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [suggestedTerm:null,errors:["Cannot save object:"+ex.toString()]], status : 400]
    }

  }

  def undo() {
    log.info("Undo")
    def suggestedTermData = JSON.parse(data)
    SuggestedTerm suggestedTerm = SuggestedTerm.get(suggestedTermData.id)
    def callback = [annotationID : suggestedTerm?.getIdAnnotation()]
    suggestedTerm.delete(flush:true)
    String id = suggestedTermData.id
    return super.createUndoMessage(id,suggestedTerm,[Term.read(suggestedTermData.term)?.name,Annotation.read(suggestedTermData.annotation)?.id,Job.read(suggestedTermData.job)?.software?.name] as Object[],callback);
  }

  def redo() {
    log.info("Undo")
    def suggestedTermData = JSON.parse(data)
    def suggestedTerm = SuggestedTerm.createFromData(suggestedTermData)
    suggestedTerm.id = suggestedTermData.id
    suggestedTerm.save(flush:true)
    def callback = [annotationID : suggestedTerm?.getIdAnnotation()]
    return super.createRedoMessage(suggestedTerm,[Term.read(suggestedTermData.term)?.name,Annotation.read(suggestedTermData.annotation)?.id,Job.read(suggestedTermData.job)?.software?.name] as Object[],callback);
  }

}
