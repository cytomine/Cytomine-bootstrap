package be.cytomine.command.termOntology

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.TermOntology
import be.cytomine.project.Ontology
import be.cytomine.project.Term

class EditTermOntologyCommand extends Command implements UndoRedoCommand  {


  def execute() {

    log.info "Execute"
    log.debug "postData="+postData
    def postData = JSON.parse(postData)

    log.debug "Term id="+postData.termOntology.term.id + " Ontology id=" + postData.termOntology.ontology.id

    def updatedTermOntology = TermOntology.findByTermAndOntology(Term.get(postData.termOntology.term.id),Ontology.get(postData.termOntology.ontology.id))
    def backup = updatedTermOntology.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

    if (!updatedTermOntology ) {
      log.error "Term-Ontology not found with id term: " + postData.termOntology.term.id + " and id ontology: " + postData.termOntology.ontology.id
      return [data : [success : false, message : "Term-Ontology not found with id term: " + postData.termOntology.term.id + " and id ontology: " + postData.termOntology.ontology.id], status : 404]
    }

    updatedTermOntology = TermOntology.getTermOntologyFromData(updatedTermOntology,postData.termOntology)
    updatedTermOntology.id = postData.termOntology.id


    if ( updatedTermOntology.validate() && updatedTermOntology.save()) {
      log.info "New TermOntology is saved"
      data = ([ previousTermOntology : (JSON.parse(backup)), newTermOntology :  updatedTermOntology]) as JSON
      return [data : [success : true, message:"ok", termOntology :  updatedTermOntology], status : 200]
    } else {
      log.error "New TermOntology can't be saved: " +  updatedTermOntology.errors
      return [data : [termOntology :  updatedTermOntology, errors : updatedTermOntology.retrieveErrors()], status : 400]
    }
  }

  def undo() {
    log.info "Undo"
    def termOntologyData = JSON.parse(data)
    TermOntology termOntology = TermOntology.findById(termOntologyData.previousTermOntology.id)
    termOntology = TermOntology.getTermOntologyFromData(termOntology,termOntologyData.previousTermOntology)
    termOntology.save(flush:true)
    return [data : [success : true, message:"ok", termOntology : termOntology], status : 200]
  }

  def redo() {
    log.info "Redo"
    def termOntologyData = JSON.parse(data)
    TermOntology termOntology = TermOntology.findById(termOntologyData.newTermOntology.id)
    termOntology = TermOntology.getTermOntologyFromData(termOntology,termOntologyData.newTermOntology)
    termOntology.save(flush:true)
    return [data : [success : true, message:"ok", termOntology : termOntology], status : 200]
  }
}
