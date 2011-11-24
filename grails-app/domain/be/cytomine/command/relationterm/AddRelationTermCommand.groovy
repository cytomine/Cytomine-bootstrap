package be.cytomine.command.relationterm

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Term
import grails.converters.JSON

class AddRelationTermCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Init new domain object
        RelationTerm newRelation = RelationTerm.createFromData(json)
        //Link relation domain
        newRelation = RelationTerm.link(newRelation.relation, newRelation.term1, newRelation.term2)
        //Build response message
        String message = createMessage(newRelation,[newRelation.id, newRelation.relation.name, newRelation.term1.name, newRelation.term2.name])
        //Init command info
        fillCommandInfo(newRelation,message)
        //Create and return response
        return responseService.createResponseMessage(newRelation,message,printMessage)
    }

    def undo() {
        return destroy(relationTermService,JSON.parse(data))
    }

    def redo() {
        return restore(relationTermService,JSON.parse(data))
    }

}
