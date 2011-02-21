package be.cytomine.command.relation

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Relation
import grails.converters.JSON

class AddRelationCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      Relation newRelation = Relation.createRelationFromData(json.relation)
      if (newRelation.validate()) {
        newRelation.save(flush:true)
        log.info("Save relation with id:"+newRelation.id)
        data = newRelation.encodeAsJSON()
        return [data : [success : true, message:"ok", relation : newRelation], status : 201]
      } else {
        return [data : [relation : newRelation, errors : newRelation.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save relation:"+ex.toString())
      return [data : [relation : null , errors : ["Cannot save relation:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def relationData = JSON.parse(data)
    def relation = Relation.findById(relationData.id)
    relation.delete(flush:true)
    log.debug("Delete relation with id:"+relationData.id)
    return [data : ["Relation deleted"], status : 201]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def relationData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def relation = Relation.createRelationFromData(json.relation)
    relation.id = relationData.id
    relation.save(flush:true)
    log.debug("Save relation:"+relation.id)
    return [data : [relation : relation], status : 200]
  }
}
