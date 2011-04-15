package be.cytomine.command.term

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Term
import be.cytomine.command.EditCommand

class EditTermCommand extends EditCommand implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    log.debug "postData="+postData

    try {
      def postData = JSON.parse(postData)
      log.debug "Term id="+postData.id
      def updatedTerm = Term.get(postData.id)
      def backup = updatedTerm.encodeAsJSON()

      if (!updatedTerm ) {
        log.error "Term not found with id: " + postData.id
        return [data : [success : false, message : "Term not found with id: " + postData.id], status : 404]
      }

      updatedTerm = Term.getTermFromData(updatedTerm,postData)
      updatedTerm.id = postData.id

      if ( updatedTerm.validate() && updatedTerm.save(flush:true)) {
        log.info "New Term is saved"
        data = ([ previousTerm : (JSON.parse(backup)), newTerm :  updatedTerm]) as JSON
        return [data : [success : true, message:"ok", term :  updatedTerm], status : 200]
      } else {
        log.error "New Term can't be saved: " +  updatedTerm.errors
        return [data : [term :  updatedTerm, errors : updatedTerm.retrieveErrors()], status : 400]
      }
    }
    catch(IllegalArgumentException e)
    {
      log.error "New Term can't be saved: " +  e.toString()
      return [data : [term : null , errors : [e.toString()]], status : 400]
    }
  }



  def undo() {
    log.info "Undo"
    def termData = JSON.parse(data)
    Term term = Term.findById(termData.previousTerm.id)
    term = Term.getTermFromData(term,termData.previousTerm)
    term.save(flush:true)
    return [data : [success : true, message:"ok", term : term], status : 200]
  }

  def redo() {
    log.info "Redo"
    def termData = JSON.parse(data)
    Term term = Term.findById(termData.newTerm.id)
    term = Term.getTermFromData(term,termData.newTerm)
    term.save(flush:true)
    return [data : [success : true, message:"ok", term : term], status : 200]
  }
}
