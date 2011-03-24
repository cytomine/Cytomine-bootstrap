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
import be.cytomine.ontology.Ontology
import grails.converters.JSON

class DeleteOntologyCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    Ontology ontology = Ontology.findById(postData.id)
    data = ontology.encodeAsJSON()

    if (!ontology) {
      return [data : [success : false, message : "Ontology not found with id: " + postData.id], status : 404]
    }
    try {
      ontology.delete(flush:true);
      return [data : [success : true, message : "OK", data : [ontology : postData.id]], status : 200]
    } catch(org.springframework.dao.DataIntegrityViolationException e)
    {
      log.error(e)
      return [data : [success : false, errors : "Ontology is still map with data (relation, annotation...)"], status : 400]
    }
  }

  def undo() {
    def ontologyData = JSON.parse(data)
    Ontology ontology = Ontology.createOntologyFromData(ontologyData)
    ontology.save(flush:true)
    log.error "Ontology errors = " + ontology.errors

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  ontology.id
    postData = postDataLocal.toString()

    log.debug "ontology with id " + ontology.id

    return [data : [success : true, ontology : ontology, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    Ontology ontology = Ontology.findById(postData.id)
    ontology.delete(flush:true);
    return [data : [success : true, message : "OK"], status : 200]

  }
}