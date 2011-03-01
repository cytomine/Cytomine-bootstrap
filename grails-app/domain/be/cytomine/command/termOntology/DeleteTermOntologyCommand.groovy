package be.cytomine.command.termOntology

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.TermOntology
import be.cytomine.project.Term
import grails.converters.JSON
import be.cytomine.project.Ontology

class DeleteTermOntologyCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)


      Ontology ontology = Ontology.get(postData.ontology)
      Term term = Term.get(postData.term)

    log.info "execute with ontology=" + ontology + " term=" + term
    TermOntology termOntology = TermOntology.findByOntologyAndTerm(ontology,term)
    data = termOntology.encodeAsJSON()

    if (!termOntology) {
      return [data : [success : false, message : "TermOntology not found with id: " + postData.id], status : 404]
    }
    log.info "Unlink=" + termOntology.ontology +" " + termOntology.term
    TermOntology.unlink(termOntology.term,termOntology.ontology)

    return [data : [success : true, message : "OK", data : [termOntology : postData.id]], status : 204]
  }

  def undo() {


    def termOntologyData = JSON.parse(data)
    TermOntology termOntology = TermOntology.createTermOntologyFromData(termOntologyData)
    termOntology = TermOntology.link(termOntologyData.id, termOntology)
    //termOntology.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  termOntology.id
    postData = postDataLocal.toString()

    log.debug "TermOntology with id " + termOntology.id

    return [data : [success : true, termOntology : termOntology, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)

      Ontology ontology = Ontology.get(postData.ontology)
      Term term = Term.get(postData.term)
    TermOntology termOntology = TermOntology.findByOntologyAndTerm(ontology,term)
    TermOntology.unlink(termOntology.term,termOntology.ontology)
    return [data : [success : true, message : "OK"], status : 204]

  }

}