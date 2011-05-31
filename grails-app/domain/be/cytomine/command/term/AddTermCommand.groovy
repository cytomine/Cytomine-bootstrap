package be.cytomine.command.term

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand
import be.cytomine.ontology.Ontology
import grails.validation.ValidationException
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddTermCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info("Execute")
    Term newTerm
    try {
      def json = JSON.parse(postData)
      newTerm = Term.createFromData(json)
      return super.validateAndSave(newTerm,"Term",["#ID#",json.name,Ontology.read(json.ontology)?.name] as Object[])
      //errors:
    }catch(ConstraintException  ex){
      return [data : [term:newTerm,errors:newTerm.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [term:null,errors:["Cannot save object:"+ex.toString()]], status : 400]
    }

  }

  def undo() {
    log.info("Undo AddTermCommand")
    def termData = JSON.parse(data)
    return super.undo(termData,new Term(),'Term',[termData.id,termData.name,Ontology.read(termData.ontology)?.name] as Object[]);
  }

  def redo() {
    log.info("Undo RedoTermCommand")
    def termData = JSON.parse(data)
    def json = JSON.parse(postData)
    return super.redo(termData,json,new Term(),'Term',[termData.id,termData.name,Ontology.read(termData.ontology)?.name] as Object[]);
  }

}
