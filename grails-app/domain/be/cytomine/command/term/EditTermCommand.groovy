package be.cytomine.command.term

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Term
import grails.converters.JSON

class EditTermCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve domain
        Term updatedDomain = Term.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Term ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain.name, updatedDomain.ontology?.name])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
    }

    def undo() {
        def termData = JSON.parse(data)
        Term term = Term.findById(termData.previousTerm.id)
        term = term.getFromData(term, termData.previousTerm)
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        super.createUndoMessage(termData, term, [term.id, term.name, term.ontology?.name] as Object[], callback)
    }

    def redo() {
        def termData = JSON.parse(data)
        Term term = Term.findById(termData.newTerm.id)
        term = Term.getFromData(term, termData.newTerm)
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        super.createRedoMessage(termData, term, [term.id, term.name, term.ontology?.name] as Object[], callback)
    }
}
