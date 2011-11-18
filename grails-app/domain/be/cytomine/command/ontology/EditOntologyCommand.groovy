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
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException

class EditOntologyCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        log.info "Execute"
        Ontology updatedOntology = Ontology.get(json.id)
        if(!updatedOntology) throw new ObjectNotFoundException("Ontology " + json.id + " was not found")
        return super.validateAndSave(json, updatedOntology, [updatedOntology.id, updatedOntology.name] as Object[])
    }

    def undo() {
        log.info "Undo"
        def ontologyData = JSON.parse(data)
        Ontology ontology = Ontology.findById(ontologyData.previousOntology.id)
        ontology = Ontology.getFromData(ontology, ontologyData.previousOntology)
        ontology.save(flush: true)
        super.createUndoMessage(ontologyData, ontology, [ontology.id, ontology.name] as Object[])
    }

    def redo() {
        log.info "Redo"
        def ontologyData = JSON.parse(data)
        Ontology ontology = Ontology.findById(ontologyData.newOntology.id)
        ontology = Ontology.getFromData(ontology, ontologyData.newOntology)
        ontology.save(flush: true)
        super.createRedoMessage(ontologyData, ontology, [ontology.id, ontology.name] as Object[])
    }

}