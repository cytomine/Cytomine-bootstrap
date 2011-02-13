package be.cytomine.command.term

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.project.Term

class AddTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    def newTerm = Term.getTermFromData(JSON.parse(postData))
    if (newTerm.validate()) {
      newTerm.save(flush:true)
      data = newTerm.encodeAsJSON()
      return [data : [success : true, message:"ok", term : newTerm], status : 201]
    } else {
      return [data : [term : newTerm, errors : [newTerm.errors]], status : 400]
    }
  }

  def undo() {
    def termData = JSON.parse(data)
    def term = Term.get(termData.id)
    term.delete()
    return [data : null, status : 200]
  }

  def redo() {
    def termData = JSON.parse(data)
    def term = Term.getTermFromData(JSON.parse(postData))
    term.id = termData.id
    println "redo term.id=" + term.id   + " term.validate()"+term.validate()
    term.save(flush:true)
    //return [data : [term : term], status : 200]
    println "response"
    return [data : "ibiza", status : 200]
  }
}
