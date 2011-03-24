package be.cytomine.command.relationterm

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term

class AddRelationTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      RelationTerm newRelationTerm = RelationTerm.createRelationTermFromData(json.relationTerm)
      if (newRelationTerm.validate()) {
        newRelationTerm = RelationTerm.link(newRelationTerm.relation,newRelationTerm.term1,newRelationTerm.term2)
        //newRelationTerm.save(flush:true)
        log.info("Save RelationTerm with id:"+newRelationTerm.id)
        data = newRelationTerm.encodeAsJSON()
        return [data : [success : true, message:"ok", relationTerm : newRelationTerm], status : 201]
      } else {
        return [data : [relationTerm : newRelationTerm, errors : newRelationTerm.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save relationTerm:"+ex.toString())
      return [data : [relationTerm : null , errors : ["Cannot save relationTerm:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def relationTermData = JSON.parse(data)
    def relationTerm = RelationTerm.findWhere('relation': Relation.get(relationTermData.relation.id),'term1':Term.get(relationTermData.term1.id), 'term2':Term.get(relationTermData.term2.id))
    RelationTerm.unlink(relationTerm.relation,relationTerm.term1,relationTerm.term2)
    log.debug("Delete relationTerm with id:"+relationTermData.id)
    return [data : ["RelationTerm deleted"], status : 200]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def relationTermData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def relationTerm = RelationTerm.createRelationTermFromData(json.relationTerm)
    relationTerm = RelationTerm.link(relationTermData.id,relationTerm.relation,relationTerm.term1,relationTerm.term2)

    log.debug("Save relationTerm:"+relationTerm.id)
    return [data : [relationTerm : relationTerm], status : 201]
  }

}
