package be.cytomine.command.relationterm

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand
import be.cytomine.Exception.ObjectNotFoundException

class AddRelationTermCommand extends AddCommand implements UndoRedoCommand {

  boolean saveOnUndoRedoStack = true;

  def execute() {
      RelationTerm newRelationTerm = RelationTerm.createFromData(json)
      newRelationTerm = RelationTerm.link(newRelationTerm.relation,newRelationTerm.term1,newRelationTerm.term2)
      if(!newRelationTerm) throw new ObjectNotFoundException("RelationTerm $newRelationTerm.relation - ($newRelationTerm.term1#$newRelationTerm.term2) not found")
      return super.validateWithoutSave(newRelationTerm,["#ID#",newRelationTerm.relation.name,newRelationTerm.term1.name,newRelationTerm.term2.name] as Object[])
  }

  def undo() {
    log.info("Undo")
    def relationTermData = JSON.parse(data)
    def relationTerm = RelationTerm.findWhere(
            'relation': Relation.get(relationTermData.relation.id),
            'term1':Term.get(relationTermData.term1.id),
            'term2':Term.get(relationTermData.term2.id)
    )
    Relation relation = relationTerm.relation
    Term term1 =   relationTerm.term1
    Term term2 = relationTerm.term2
    RelationTerm.unlink(relationTerm.relation,relationTerm.term1,relationTerm.term2)
    String id = relationTermData.id
    return super.createUndoMessage(id, relationTerm,[relationTermData.id,relation.name,term1.name,term2.name] as Object[]);
  }

  def redo() {
    log.info("Redo")
    def relationTermData = JSON.parse(data)
    def relationTerm = RelationTerm.createFromData(relationTermData)
    relationTerm = RelationTerm.link(relationTermData.id,relationTerm.relation,relationTerm.term1,relationTerm.term2)
    relationTerm.id = relationTermData.id
    relationTerm.save(flush:true)
    return super.createRedoMessage(relationTerm,[relationTermData.id,relationTerm.relation.name,relationTerm.term1.name,relationTerm.term2.name] as Object[]);
  }

}
