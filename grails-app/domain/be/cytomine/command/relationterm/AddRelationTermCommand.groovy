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
        log.info("Undo")
        def relationTermData = JSON.parse(data)
        def relationTerm = RelationTerm.findWhere(
                'relation': Relation.get(relationTermData.relation.id),
                'term1': Term.get(relationTermData.term1.id),
                'term2': Term.get(relationTermData.term2.id)
        )
        Relation relation = relationTerm.relation
        Term term1 = relationTerm.term1
        Term term2 = relationTerm.term2
        RelationTerm.unlink(relationTerm.relation, relationTerm.term1, relationTerm.term2)
        String id = relationTermData.id
        return super.createUndoMessage(id, relationTerm, [relationTermData.id, relation.name, term1.name, term2.name] as Object[]);
    }

    def redo() {
        log.info("Redo")
        def relationTermData = JSON.parse(data)
        def relationTerm = RelationTerm.createFromData(relationTermData)
        relationTerm = RelationTerm.link(relationTermData.id, relationTerm.relation, relationTerm.term1, relationTerm.term2)
        relationTerm.id = relationTermData.id
        relationTerm.save(flush: true)
        return super.createRedoMessage(relationTerm, [relationTermData.id, relationTerm.relation.name, relationTerm.term1.name, relationTerm.term2.name] as Object[]);
    }

}
