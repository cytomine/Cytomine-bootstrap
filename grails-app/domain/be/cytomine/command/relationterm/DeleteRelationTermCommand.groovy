package be.cytomine.command.relationterm

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Term
import grails.converters.JSON

class DeleteRelationTermCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve domain
        Relation relation = Relation.get(json.relation)
        Term term1 = Term.get(json.term1)
        Term term2 = Term.get(json.term2)
        RelationTerm relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) throw new ObjectNotFoundException("Relation-Term not found with 'relation'=$relation,'term1'=$term1, 'term2'=$term2")
        String id = relation.id
         //Build response message
        String message = createMessage(relationTerm, [relationTerm.id, relation.name, term1.name, term2.name])
         //Init command info
        fillCommandInfo(relationTerm,message)
        //Delete domain
        RelationTerm.unlink(relationTerm.relation, relationTerm.term1, relationTerm.term2)
        //Create and return response
        return responseService.createResponseMessage(relationTerm,message,printMessage)
    }

    def undo() {
        log.info("Undo")
        def relationTermData = JSON.parse(data)
        RelationTerm relationTerm = RelationTerm.createFromData(relationTermData)
        relationTerm = RelationTerm.link(relationTermData.id, relationTerm.relation, relationTerm.term1, relationTerm.term2)
        return super.createUndoMessage(relationTerm, [relationTerm.id, relationTerm.relation?.name, relationTerm.term1?.name, relationTerm.term2?.name] as Object[]);
    }

    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        Relation relation = Relation.get(postData.relation)
        Term term1 = Term.get(postData.term1)
        Term term2 = Term.get(postData.term2)

        RelationTerm relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        String id = relationTerm
        RelationTerm.unlink(relationTerm.relation, relationTerm.term1, relationTerm.term2)

        return super.createRedoMessage(id, relationTerm, [relationTerm.id, relation.name, term1.name, term2.name] as Object[]);
    }
}