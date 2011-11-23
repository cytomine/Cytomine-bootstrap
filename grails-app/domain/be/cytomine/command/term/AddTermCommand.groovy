package be.cytomine.command.term

import be.cytomine.Exception.CytomineException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import grails.converters.JSON

class AddTermCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Init new domain object
        Term domain = Term.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name,domain.ontology?.name])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        //Get object to delete
        Term term = Term.get(JSON.parse(data).id)
        //Build response message
        def response = createResponseMessageUndo(term,[term.id, term.name, term.ontology.name],[ontologyID: term?.ontology?.id])
        //Delete object
        term.delete(flush: true)
        return response
    }

    def redo() {
        //Rebuilt object that was previoulsy deleted
        def term = Term.createFromDataWithId(JSON.parse(data))
        //Build response message
        def response = createResponseMessageRedo(term,[term.id, term.name, term.ontology.name],[ontologyID: term?.ontology?.id])
        //Save new object
        term.save(flush: true)
        return response
    }
}
