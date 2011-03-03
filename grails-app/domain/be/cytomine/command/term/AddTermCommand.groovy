package be.cytomine.command.term

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.Term

class AddTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      Term newTerm = Term.createTermFromData(json.term)
      if (newTerm.validate()) {
        newTerm.save(flush:true)
        log.info("Save term with id:"+newTerm.id)
        data = newTerm.encodeAsJSON()
        return [data : [success : true, message:"ok", term : newTerm], status : 201]
      } else {
        return [data : [term : newTerm, errors : newTerm.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save term:"+ex.toString())
      return [data : [term : null , errors : ["Cannot save term:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def termData = JSON.parse(data)
    def term = Term.findById(termData.id)
    term.delete(flush:true)
    log.debug("Delete term with id:"+termData.id)
    return [data : ["Term deleted"], status : 200]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def termData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def term = Term.createTermFromData(json.term)
    term.id = termData.id
    term.save(flush:true)
    log.debug("Save term:"+term.id)
    return [data : [term : term], status : 201]
  }
}
