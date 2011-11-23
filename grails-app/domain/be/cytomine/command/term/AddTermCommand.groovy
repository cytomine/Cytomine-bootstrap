package be.cytomine.command.term

import be.cytomine.Exception.CytomineException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import grails.converters.JSON

class AddTermCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
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
        def termData = JSON.parse(data)
        Term term = Term.get(termData.id)
        def callback = [ontologyID: term?.ontology?.id]
        term.delete(flush: true)
        String id = termData.id
        return super.createUndoMessage(id, term, [termData.id, termData.name, Ontology.read(termData.ontology).name] as Object[], callback);
    }

    def redo() {
        def termData = JSON.parse(data)
        def term = Term.createFromData(termData)
        term.id = termData.id
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        return super.createRedoMessage(term, [termData.id, termData.name, Ontology.read(termData.ontology)?.name] as Object[], callback);
    }

}
