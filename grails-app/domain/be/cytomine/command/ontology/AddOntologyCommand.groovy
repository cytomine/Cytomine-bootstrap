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

class AddOntologyCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      Ontology newOntology = Ontology.createOntologyFromData(json.ontology)
      if (newOntology.validate()) {
        newOntology.save(flush:true)
        log.info("Save ontology with id:"+newOntology.id)
        data = newOntology.encodeAsJSON()
        return [data : [success : true, message:"ok", ontology : newOntology], status : 201]
      } else {
        return [data : [ontology : newOntology, errors : newOntology.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save ontology:"+ex.toString())
      return [data : [ontology : null , errors : ["Cannot save ontology:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def ontologyData = JSON.parse(data)
    def ontology = Ontology.findById(ontologyData.id)
    ontology.delete(flush:true)
    log.debug("Delete ontology with id:"+ontologyData.id)
    return [data : ["Ontology deleted"], status : 200]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def ontologyData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def ontology = Ontology.createOntologyFromData(json.ontology)
    ontology.id = ontologyData.id
    ontology.save(flush:true)
    log.debug("Save ontology:"+ontology.id)
    return [data : [ontology : ontology], status : 201]
  }
}
