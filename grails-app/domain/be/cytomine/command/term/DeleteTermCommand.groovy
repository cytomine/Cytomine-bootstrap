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
        def termData = JSON.parse(data)
        Term term = Term.createFromData(termData)
        term.id = termData.id;
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        return super.createUndoMessage(term, [term.id, term.name, term.ontology] as Object[], callback);
    }

    def redo() {
        def termData = JSON.parse(postData)
        Term term = Term.findById(termData.id)
        String id = termData.id
        String name = term.name
        String ontologyName = term.ontology?.name
        def callback = [ontologyID: term?.ontology?.id]
        term.delete(flush: true);
        return super.createRedoMessage(id, term, [id, name, ontologyName] as Object[], callback);
    }

}
