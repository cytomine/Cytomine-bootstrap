package be.cytomine.command.ontology

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 */

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Ontology
import grails.converters.JSON

class EditOntologyCommand extends EditCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        //Retrieve domain
        Ontology updatedDomain = Ontology.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Ontology ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain.name])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
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