package be.cytomine.command.relation

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Relation
import grails.converters.JSON
import be.cytomine.command.DeleteCommand

class DeleteRelationCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {  //must be refactor with deleteCommand
    def postData = JSON.parse(postData)

    Relation relation = Relation.findById(postData.id)
    data = relation.encodeAsJSON()

    if (!relation) {
      return [data : [success : false, message : "Relation not found with id: " + postData.id], status : 404]
    }
    try {
      relation.delete(flush:true);
      return [data : [success : true, message : "OK", data : [relation : postData.id]], status : 200]
    } catch(org.springframework.dao.DataIntegrityViolationException e)
    {
      log.error(e)
      return [data : [success : false, errors : "Relation is still map with data (term,...)"], status : 400]
    }
  }

  def undo() {
    def relationData = JSON.parse(data)
    Relation relation = Relation.createRelationFromData(relationData)
    relation.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  relation.id
    postData = postDataLocal.toString()

    log.debug "relation with id " + relation.id

    return [data : [success : true, relation : relation, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    Relation relation = Relation.findById(postData.id)
    relation.delete(flush:true);
    return [data : [success : true, message : "OK"], status : 200]

  }
}
