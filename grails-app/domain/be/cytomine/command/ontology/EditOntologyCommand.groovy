package be.cytomine.command.ontology

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Ontology
import be.cytomine.command.EditCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class EditOntologyCommand extends EditCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    log.debug "postData="+postData
    Ontology updatedOntology=null
    try {
      def postData = JSON.parse(postData)
      updatedOntology = Ontology.get(postData.id)

      return super.validateAndSave(postData,updatedOntology,[updatedOntology.id,updatedOntology.name] as Object[])

    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(ConstraintException e) {
      log.error(e)
      return [data : [success : false, errors : updatedOntology.retrieveErrors()], status : 400]
    } catch(IllegalArgumentException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    }

  }

  def undo() {
    log.info "Undo"
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.findById(ontologyData.previousOntology.id)
    ontology = Ontology.getFromData(ontology,ontologyData.previousOntology)
    ontology.save(flush:true)
    super.createUndoMessage(ontologyData, ontology,[ontology.id,ontology.name] as Object[])
  }

  def redo() {
    log.info "Redo"
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.findById(ontologyData.newOntology.id)
    ontology = Ontology.getFromData(ontology,ontologyData.newOntology)
    ontology.save(flush:true)
    super.createRedoMessage(ontologyData, ontology,[ontology.id,ontology.name] as Object[])
  }

}