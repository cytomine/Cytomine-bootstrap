package be.cytomine.command.suggestedTerm

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import grails.converters.JSON

class AddSuggestedTermCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Init new domain object
        SuggestedTerm newRelation = SuggestedTerm.createFromData(json)
        domainService.saveDomain(newRelation)
        //Build response message
        String message = createMessage(newRelation,[newRelation.id, newRelation.term.name,newRelation.annotation.id , newRelation.job?.software?.name])
        //Init command info
        fillCommandInfo(newRelation,message)
        //Create and return response
        super.initCurrentCommantProject(newRelation.annotation.image.project)
        return responseService.createResponseMessage(newRelation,message,printMessage)
    }

    def undo() {
        return destroy(suggestedTermService,JSON.parse(data))
    }

    def redo() {
        return restore(suggestedTermService,JSON.parse(data))
    }

}
