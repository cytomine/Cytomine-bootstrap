package be.cytomine.command.suggestedTerm

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import grails.converters.JSON

class DeleteSuggestedTermCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve domain
        Annotation annotation = Annotation.read(json.annotation)
        Term term = Term.read(json.term)
        Job job = Job.read(json.job)
        SuggestedTerm domain = SuggestedTerm.findWhere(annotation: annotation, term: term, job: job)
        if (!domain) throw new ObjectNotFoundException("SuggestedTerm was not found with annotation:$annotation,term:$term,job:$job")
        //Build response message
        String message = createMessage(domain, [domain.term.name, domain.annotation.id, domain.job.id])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return restore(suggestedTermService,JSON.parse(data))
    }

    def redo() {
        return destroy(suggestedTermService,JSON.parse(data))
    }

}
