package be.cytomine.command.ontology

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Ontology
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddOntologyCommand extends AddCommand implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    Ontology newOntology
    try{
      def json = JSON.parse(postData)
      newOntology = Ontology.createOntologyFromData(json)
      return super.validateAndSave(newOntology,"Ontology",["#ID#",json.name] as Object[])
      //errors:
    }catch(ConstraintException  ex){
      return [data : [ontology:newOntology,errors:newOntology.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [ontology:null,errors:["Cannot save ontology:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.get(ontologyData.id)
    ontology.delete(flush:true)

    log.info ("termData="+ontologyData)

    String id = ontologyData.id

    return super.createUndoMessage(
            id,
            'Ontology',
            [ontologyData.id,ontologyData.name] as Object[]
    );
  }

  def redo() {
    log.info("Undo")
    def ontologyData = JSON.parse(data)
    def json = JSON.parse(postData)
    def ontology = Ontology.createOntologyFromData(ontologyData)
    ontology.id = ontologyData.id
    ontology.save(flush:true)
    return super.createRedoMessage(
            ontology,
            'Ontology',
            [ontologyData.id,ontologyData.name] as Object[]
    );
  }

}
