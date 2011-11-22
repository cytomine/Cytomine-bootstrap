package be.cytomine.command.suggestedTerm

import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Term
import grails.converters.JSON
import java.util.prefs.BackingStoreException
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Annotation
import be.cytomine.processing.Job
import be.cytomine.Exception.ObjectNotFoundException

class DeleteSuggestedTermCommand extends DeleteCommand implements UndoRedoCommand {

  boolean saveOnUndoRedoStack = true;

  def execute() {
      Annotation annotation = Annotation.read(json.annotation)
      Term term = Term.read(json.term)
      Job job = Job.read(json.job)
      SuggestedTerm suggestedTerm = SuggestedTerm.findWhere(annotation:annotation,term:term,job:job)
      if(!suggestedTerm) throw new ObjectNotFoundException("SuggestedTerm was not found with annotation:$annotation,term:$term,job:$job")
      String id = suggestedTerm.id
      return super.deleteAndCreateDeleteMessage(id,suggestedTerm,[suggestedTerm.term.name,suggestedTerm.annotation.id,suggestedTerm.job.id] as Object[])
  }

  def undo() {
    log.info("Undo")
    def suggestedTermData = JSON.parse(data)
    SuggestedTerm suggestedTerm = SuggestedTerm.createFromData(suggestedTermData)
    suggestedTerm.id = suggestedTermData.id;
    suggestedTerm.save(flush:true)
    def callback = [annotationID : suggestedTerm?.getIdAnnotation()]
    return super.createUndoMessage(suggestedTerm,[suggestedTerm?.term?.name,suggestedTerm.getIdAnnotation(),suggestedTerm.getIdJob()] as Object[],callback);
  }

  def redo() {
    log.info("Redo")
    def suggestedTermData = JSON.parse(postData)
    SuggestedTerm suggestedTerm = SuggestedTerm.findById(suggestedTermData.id)
    String id = suggestedTermData.id
    String annotationId = suggestedTerm.getIdAnnotation()
    String termName = suggestedTerm?.term?.name
    String jobName = suggestedTerm?.job?.software?.name
    def callback = [annotationID : suggestedTerm?.getIdAnnotation()]
    suggestedTerm.delete(flush:true);
    return super.createRedoMessage(id,suggestedTerm,[termName,annotationId,jobName] as Object[],callback);
  }

}
