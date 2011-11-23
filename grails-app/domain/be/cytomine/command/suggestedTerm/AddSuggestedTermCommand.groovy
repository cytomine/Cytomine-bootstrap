package be.cytomine.command.suggestedTerm

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import grails.converters.JSON

class AddSuggestedTermCommand extends AddCommand implements UndoRedoCommand {

    def domainService

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
        log.info("Undo")
        def suggestedTermData = JSON.parse(data)
        SuggestedTerm suggestedTerm = SuggestedTerm.get(suggestedTermData.id)
        def callback = [annotationID: suggestedTerm?.getIdAnnotation()]
        suggestedTerm.delete(flush: true)
        String id = suggestedTermData.id
        return super.createUndoMessage(id, suggestedTerm, [Term.read(suggestedTermData.term)?.name, Annotation.read(suggestedTermData.annotation)?.id, Job.read(suggestedTermData.job)?.software?.name] as Object[], callback);
    }

    def redo() {
        log.info("Redo")
        def suggestedTermData = JSON.parse(data)
        def suggestedTerm = SuggestedTerm.createFromData(suggestedTermData)
        suggestedTerm.id = suggestedTermData.id
        suggestedTerm.save(flush: true)
        def callback = [annotationID: suggestedTerm?.getIdAnnotation()]
        return super.createRedoMessage(suggestedTerm, [Term.read(suggestedTermData.term)?.name, Annotation.read(suggestedTermData.annotation)?.id, Job.read(suggestedTermData.job)?.software?.name] as Object[], callback);
    }

}
