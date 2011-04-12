package be.cytomine.command.relation

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Relation

class EditRelationCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    log.debug "postData="+postData

    try {
      def postData = JSON.parse(postData)
      log.debug "Relation id="+postData.id
      def updatedRelation = Relation.get(postData.id)
      def backup = updatedRelation.encodeAsJSON()

      if (!updatedRelation ) {
        log.error "Relation not found with id: " + postData.id
        return [data : [success : false, message : "Relation not found with id: " + postData.id], status : 404]
      }

      updatedRelation = Relation.getRelationFromData(updatedRelation,postData)
      updatedRelation.id = postData.id

      if ( updatedRelation.validate() && updatedRelation.save(flush:true)) {
        log.info "New Relation is saved"
        data = ([ previousRelation : (JSON.parse(backup)), newRelation :  updatedRelation]) as JSON
        return [data : [success : true, message:"ok", relation :  updatedRelation], status : 200]
      } else {
        log.error "New Relation can't be saved: " +  updatedRelation.errors
        return [data : [relation :  updatedRelation, errors :  updatedRelation.retrieveErrors()], status : 400]
      }
    }
    catch(IllegalArgumentException e)
    {
      log.error "New Relation can't be saved: " +  e.toString()
      return [data : [relation : null , errors : [e.toString()]], status : 400]
    }
  }



  def undo() {
    log.info "Undo"
    def relationData = JSON.parse(data)
    Relation relation = Relation.findById(relationData.previousRelation.id)
    relation = Relation.getRelationFromData(relation,relationData.previousRelation)
    relation.save(flush:true)
    return [data : [success : true, message:"ok", relation : relation], status : 200]
  }

  def redo() {
    log.info "Redo"
    def relationData = JSON.parse(data)
    Relation relation = Relation.findById(relationData.newRelation.id)
    relation = Relation.getRelationFromData(relation,relationData.newRelation)
    relation.save(flush:true)
    return [data : [success : true, message:"ok", relation : relation], status : 200]
  }





}
