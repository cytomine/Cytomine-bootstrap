package be.cytomine.command.relation

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Relation
import grails.converters.JSON
import be.cytomine.command.AddCommand

class AddRelationCommand extends AddCommand implements UndoRedoCommand {

  def execute() {   //must be refactored with AddCommand
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      Relation newRelation = Relation.createRelationFromData(json)
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

  }

  def redo() {

  }
}
