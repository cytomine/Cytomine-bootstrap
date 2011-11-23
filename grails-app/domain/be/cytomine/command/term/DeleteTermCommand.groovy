package be.cytomine.command.term

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Term
import grails.converters.JSON

class DeleteTermCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve domain
        Term domain = Term.get(json.id)
        if (!domain) throw new ObjectNotFoundException("Term " + json.id + " was not found")
        if (!SuggestedTerm.findAllByTerm(domain).isEmpty()) throw new ConstraintException("Term " + json.id + " has suggested term")
        if (!AnnotationTerm.findAllByTerm(domain).isEmpty()) throw new ConstraintException("Term " + json.id + " has annotation term")
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name, domain.ontology?.name])
         //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        //Rebuilt object that was previoulsy deleted
        Term term = Term.createFromDataWithId(JSON.parse(data))
        //Build response message
        def response = createResponseMessageUndo(term,[term.id, term.name, term.ontology.name],[ontologyID: term?.ontology?.id])
        //Save new object
        term.save(flush: true)
        return response;
    }

    def redo() {
        //Get object to re-delete
        Term term = Term.findById(JSON.parse(postData).id)
        //Build response message
        def response = createResponseMessageRedo(term,[term.id, term.name, term.ontology.name],[ontologyID: term?.ontology?.id])
        //Delete object
        term.delete(flush: true);
        return response
    }

}
