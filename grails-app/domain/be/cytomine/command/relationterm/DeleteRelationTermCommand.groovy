package be.cytomine.command.relationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import be.cytomine.command.DeleteCommand

class DeleteRelationTermCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    Relation relation = Relation.get(postData.relation)
    Term term1 = Term.get(postData.term1)
    Term term2 = Term.get(postData.term2)

    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)

    data = relationTerm.encodeAsJSON()

    if (!relationTerm) {
      return [data : [success : false, message : "RelationTerm not found with relation:" + postData.relation + " term1:" + postData.term1 +  "term2:" + postData.term2], status : 404]
    }
    RelationTerm.unlink(relationTerm.relation, relationTerm.term1,relationTerm.term2)
    return [data : [success : true, message : "OK", data : [relationTerm : postData.id]], status : 200]
  }

  def undo() {
    def relationTermData = JSON.parse(data)
    RelationTerm relationTerm = RelationTerm.createRelationTermFromData(relationTermData)
    relationTerm = RelationTerm.link(relationTermData.id,relationTerm.relation, relationTerm.term1,relationTerm.term2)
    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  relationTerm.id
    postData = postDataLocal.toString()

    log.debug "RelationTerm with id " + relationTerm.id

    return [data : [success : true, relationTerm : relationTerm, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    Relation relation = Relation.get(postData.relation)
    Term term1 = Term.get(postData.term1)
    Term term2 = Term.get(postData.term2)
    RelationTerm relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    RelationTerm.unlink(relationTerm.relation, relationTerm.term1, relationTerm.term2)
    return [data : [success : true, message : "OK"], status : 200]

  }

}