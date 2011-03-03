package be.cytomine.command.term

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Term
import grails.converters.JSON

class DeleteTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    Term term = Term.findById(postData.id)
    data = term.encodeAsJSON()

    if (!term) {
      return [data : [success : false, message : "Term not found with id: " + postData.id], status : 404]
    }

    term.delete();
    return [data : [success : true, message : "OK", data : [term : postData.id]], status : 200]
  }

  def undo() {
    def termData = JSON.parse(data)
    Term term = Term.createTermFromData(termData)
    term.save(flush:true)
    log.error "Term errors = " + term.errors

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  term.id
    postData = postDataLocal.toString()

    log.debug "term with id " + term.id

    return [data : [success : true, term : term, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    Term term = Term.findById(postData.id)
    term.delete(flush:true);
    return [data : [success : true, message : "OK"], status : 200]

  }
}
