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
import org.hibernate.exception.ConstraintViolationException
import java.sql.SQLException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException

class DeleteOntologyCommand extends DeleteCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() throws CytomineException{
    log.info "Execute"
      Ontology ontology = Ontology.get(json.id)
      if(!ontology) throw new ObjectNotFoundException("Ontology " + id + " not found")
      if(ontology && Project.findAllByOntology(ontology).size()>0) throw new ConstraintException("Ontology is still map with project")
      return super.deleteAndCreateDeleteMessage(json.id,ontology,[ontology.id,ontology.name] as Object[])
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
    String id = postData.id
    String name = ontology.name
    ontology.delete(flush:true);
    return super.createRedoMessage(id, ontology,[id,name] as Object[]);
  }
}