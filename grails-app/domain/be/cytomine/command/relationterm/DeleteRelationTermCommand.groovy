package be.cytomine.command.relationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.RelationTerm

class DeleteRelationTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    RelationTerm relationTerm = RelationTerm.findById(postData.id)
    data = relationTerm.encodeAsJSON()

    if (!relationTerm) {
      return [data : [success : false, message : "RelationTerm not found with id: " + postData.id], status : 404]
    }
    RelationTerm.unlink(relationTerm.relation, relationTerm.term1,relationTerm.term2)
    return [data : [success : true, message : "OK", data : [relationTerm : postData.id]], status : 200]
  }

  def undo() {
    def relationTermData = JSON.parse(data)
    RelationTerm relationTerm = RelationTerm.createRelationTermFromData(relationTermData)
    relationTerm.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  relationTerm.id
    postData = postDataLocal.toString()
    RelationTerm.link(relationTerm.id,relationTerm.relation, relationTerm.term1, relationTerm.term2)
    log.debug "RelationTerm with id " + relationTerm.id

    return [data : [success : true, relationTerm : relationTerm, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    RelationTerm relationTerm = RelationTerm.findById(postData.id)
    RelationTerm.unlink(relationTerm.relation, relationTerm.term1, relationTerm.term2)
    return [data : [success : true, message : "OK"], status : 200]

  }

}