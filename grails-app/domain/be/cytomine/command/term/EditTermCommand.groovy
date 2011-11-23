package be.cytomine.command.term

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
        Term term = fillDomainWithData(new Term(),JSON.parse(data).previousTerm)
        def response = createResponseMessageUndo(term,[term.id, term.name, term.ontology.name],[ontologyID: term?.ontology?.id])
        term.save(flush: true)
        return response
    }

    def redo() {
        Term term = fillDomainWithData(new Term(), JSON.parse(data).newTerm)
        def response = createResponseMessageRedo(term,[term.id, term.name, term.ontology.name],[ontologyID: term?.ontology?.id])
        term.save(flush: true)
        return response
    }
}
