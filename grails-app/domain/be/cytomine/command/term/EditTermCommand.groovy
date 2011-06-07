package be.cytomine.command.term

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Term
import be.cytomine.command.EditCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class EditTermCommand extends EditCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    Term updatedTerm=null
    try {
      def postData = JSON.parse(postData)
      updatedTerm = Term.get(postData.id)
      return super.validateAndSave(postData,updatedTerm,[updatedTerm.id,updatedTerm.name,updatedTerm.ontology?.name] as Object[])
    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(ConstraintException e) {
      log.error(e)
      return [data : [success : false, errors : updatedTerm.retrieveErrors()], status : 400]
    } catch(IllegalArgumentException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    }

  }

  def undo() {
    log.info "Undo"
    def termData = JSON.parse(data)
    Term term = Term.findById(termData.previousTerm.id)
    term = term.getFromData(term,data.previousTerm)
    term.save(flush:true)
    super.createUndoMessage(termData, term, [term.id,term.name,term.ontology?.name] as Object[])
  }

  def redo() {
    log.info "Redo"
    def termData = JSON.parse(data)
    Term term = Term.findById(termData.newTerm.id)
    term = Term.getFromData(term,termData.newTerm)
    term.save(flush:true)
    super.createRedoMessage(termData, term,[term.id,term.name,term.ontology?.name] as Object[])
  }
}
