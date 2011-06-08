package be.cytomine.command.relationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException

class DeleteRelationTermCommand extends DeleteCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    try {
      def postData = JSON.parse(postData)

      Relation relation = Relation.get(postData.relation)
      Term term1 = Term.get(postData.term1)
      Term term2 = Term.get(postData.term2)

      def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)

      String id = relationTerm.id
      def response = super.createDeleteMessage(id,relationTerm,[relationTerm.id,relation.name,term1.name,term2.name] as Object[])
      RelationTerm.unlink(relationTerm.relation, relationTerm.term1,relationTerm.term2)
      return response
    } catch (NullPointerException e) {
      log.error(e)
      return [data: [success: false, errors: e.getMessage()], status: 404]
    } catch (BackingStoreException e) {
      log.error(e)
      return [data: [success: false, errors: e.getMessage()], status: 400]
    }
  }

 /* def undo() {
    log.info("Undo")
    def relationTermData = JSON.parse(data)
    RelationTerm relationTerm = RelationTerm.createFromData(relationTermData)
    Relation relation =  Relation.get(relationTerm.relation)
    Term term1 = Term.get(relationTerm.term1)
    Term term2 = Term.get(relationTerm.term2)

    relationTerm = RelationTerm.link(relationTermData.id,relation, term1,term2)
    return super.createUndoMessage(relationTerm,[relationTerm.id, relation.name,term1.name,term2.name] as Object[]);
  } */
  def undo() {
    log.info("Undo")
    def relationTermData = JSON.parse(data)
    RelationTerm relationTerm = RelationTerm.createFromData(relationTermData)

    relationTerm = RelationTerm.link(relationTermData.id,relationTerm.relation, relationTerm.term1,relationTerm.term2)
    return super.createUndoMessage(relationTerm,[relationTerm.id, relationTerm.relation.name,relationTerm.term1.name,relationTerm.term2.name] as Object[]);
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Relation relation = Relation.get(postData.relation)
    Term term1 = Term.get(postData.term1)
    Term term2 = Term.get(postData.term2)

    RelationTerm relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    String id =  relationTerm
    RelationTerm.unlink(relationTerm.relation, relationTerm.term1, relationTerm.term2)

    return super.createRedoMessage(id,relationTerm,[relationTerm.id, relation.name,term1.name,term2.name] as Object[]);
  }
}