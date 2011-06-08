package be.cytomine.command.relationterm

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddRelationTermCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info("Execute")
    RelationTerm newRelationTerm=null
    try  {
      def json = JSON.parse(postData)
      newRelationTerm = RelationTerm.createFromData(json)
      newRelationTerm = RelationTerm.link(newRelationTerm.relation,newRelationTerm.term1,newRelationTerm.term2)

      return super.validateWithoutSave(
              newRelationTerm,["#ID#",newRelationTerm.relation.name,newRelationTerm.term1.name,newRelationTerm.term2.name] as Object[]
      )

    }catch(ConstraintException  ex){
      return [data : [relationterm:newRelationTerm,errors:newRelationTerm.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [relationterm:null,errors:["Cannot save relation-term:"+ex.toString()]], status : 400]
    }
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

    log.info ("relationTermData="+relationTermData)

    String id = relationTermData.id

    return super.createUndoMessage(id, relationTerm,[relationTermData.id,relation.name,term1.name,term2.name] as Object[]);
  }

  def redo() {
    log.info("Undo data="+data)
    def relationTermData = JSON.parse(data)
    def relationTerm = RelationTerm.createFromData(relationTermData)
    relationTerm = RelationTerm.link(relationTermData.id,relationTerm.relation,relationTerm.term1,relationTerm.term2)
    relationTerm.id = relationTermData.id
    relationTerm.save(flush:true)
    return super.createRedoMessage(relationTerm,[relationTermData.id,relationTerm.relation.name,relationTerm.term1.name,relationTerm.term2.name] as Object[]);
  }

}
