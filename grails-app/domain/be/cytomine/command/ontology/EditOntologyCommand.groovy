package be.cytomine.command.ontology

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Ontology

class EditOntologyCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    log.debug "postData="+postData

    try {
      def postData = JSON.parse(postData)
      log.debug "Ontology id="+postData.id
      def updatedOntology = Ontology.get(postData.id)
      def backup = updatedOntology.encodeAsJSON()

      if (!updatedOntology ) {
        log.error "Ontology not found with id: " + postData.id
        return [data : [success : false, message : "Ontology not found with id: " + postData.id], status : 404]
      }

      updatedOntology = Ontology.getOntologyFromData(updatedOntology,postData)
      updatedOntology.id = postData.id
      log.info "updatedOntology=" + updatedOntology
      log.info "updatedOntology.id=" + updatedOntology.id
      log.info "updatedOntology.name=" + updatedOntology.name
      if ( updatedOntology.validate() && updatedOntology.save(flush:true)) {
        log.info "New Ontology is saved"
        data = ([ previousOntology : (JSON.parse(backup)), newOntology :  updatedOntology]) as JSON
        return [data : [success : true, message:"ok", ontology :  updatedOntology], status : 200]
      } else {
        log.error "New Ontology can't be saved: " +  updatedOntology.errors
        return [data : [ontology :  updatedOntology, errors : updatedOntology.retrieveErrors()], status : 400]
      }
    }
    catch(IllegalArgumentException e)
    {
      log.error "New Ontology can't be saved: " +  e.toString()
      return [data : [ontology : null , errors : [e.toString()]], status : 400]
    }
  }



  def undo() {
    log.info "Undo"
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.findById(ontologyData.previousOntology.id)
    ontology = Ontology.getOntologyFromData(ontology,ontologyData.previousOntology)
    ontology.save(flush:true)
    return [data : [success : true, message:"ok", ontology : ontology], status : 200]
  }

  def redo() {
    log.info "Redo"
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.findById(ontologyData.newOntology.id)
    ontology = Ontology.getOntologyFromData(ontology,ontologyData.newOntology)
    ontology.save(flush:true)
    return [data : [success : true, message:"ok", ontology : ontology], status : 200]
  }
}