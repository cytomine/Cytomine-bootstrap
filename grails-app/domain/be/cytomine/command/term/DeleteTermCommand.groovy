package be.cytomine.command.term

import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Term
import grails.converters.JSON
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException
import be.cytomine.ontology.Ontology

class DeleteTermCommand extends DeleteCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    try {
      def postData = JSON.parse(postData)
      Term term = Term.findById(postData.id)
      return super.deleteAndCreateDeleteMessage(postData.id,term,[term.id,term.name,term.ontology?.name] as Object[])
    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(BackingStoreException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def termData = JSON.parse(data)
    Term term = Term.createFromData(termData)
    term.id = termData.id;
    term.save(flush:true)
    log.error "Term errors = " + term.errors
    return super.createUndoMessage(term,[term.id,term.name,term.ontology] as Object[]);
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Term term = Term.findById(postData.id)
    term.delete(flush:true);
    String id = postData.id
    return super.createRedoMessage(id,[postData.id,postData.name,Ontology.read(postData.ontology).name] as Object[]);
  }

}
