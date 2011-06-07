package be.cytomine.command.ontology

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Ontology
import grails.converters.JSON
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException
import be.cytomine.ontology.RelationTerm
import be.cytomine.project.Project

class DeleteOntologyCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    try {
      def postData = JSON.parse(postData)
      Ontology ontology = Ontology.findById(postData.id)
      log.info "ontology="+ontology
      if(ontology && Project.findAllByOntology(ontology).size()>0) throw new BackingStoreException("Ontology is still map with project")
      return super.deleteAndCreateDeleteMessage(postData.id,ontology,[ontology.id,ontology.name] as Object[])
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
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.createFromData(ontologyData)
    ontology.id = ontologyData.id;
    ontology.save(flush:true)
    log.error "Ontology errors = " + ontology.errors
    return super.createUndoMessage(ontology,[ontology.id,ontology.name] as Object[]);
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Ontology ontology = Ontology.findById(postData.id)
    ontology.delete(flush:true);
    String id = postData.id
    return super.createRedoMessage(id, ontology[id,postData.name] as Object[]);
  }
}