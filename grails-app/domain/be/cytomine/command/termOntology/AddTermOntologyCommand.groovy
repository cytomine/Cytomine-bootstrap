package be.cytomine.command.termOntology

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Term
import grails.converters.JSON
import be.cytomine.project.TermOntology
import be.cytomine.project.Ontology

class AddTermOntologyCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      TermOntology newTermOntology = TermOntology.createTermOntologyFromData(json.termOntology)
      if (newTermOntology.validate()) {
        newTermOntology =  TermOntology.link(newTermOntology)
        log.info("Save TermOntology with id:"+newTermOntology.id)
        data = newTermOntology.encodeAsJSON()
        return [data : [success : true, message:"ok", termOntology : newTermOntology], status : 201]
      } else {
        return [data : [termOntology : newTermOntology, errors : newTermOntology.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save termOntology:"+ex.toString())
      return [data : [termOntology : null , errors : ["Cannot save termOntology:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def termOntologyData = JSON.parse(data)
    def termOntology = TermOntology.findByOntologyAndTerm(Ontology.get(termOntologyData.ontology.id),Term.get(termOntologyData.term.id))
    TermOntology.unlink(termOntology.term,termOntology.ontology)
    log.debug("Delete termOntology with id:"+termOntologyData.id)
    return [data : ["TermOntology deleted"], status : 201]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def termOntologyData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def termOntology = TermOntology.createTermOntologyFromData(json.termOntology)
    termOntology = TermOntology.link(termOntologyData.id,termOntology)
    //println "termOntologyData.id="+termOntologyData.id

    log.debug("Save termOntology:"+termOntology.id)
    /*def session = sessionFactory.getCurrentSession()
    session.clear()     */
    //hibSession.

    return [data : [termOntology : termOntology], status : 200]
  }
  //def sessionFactory

}
