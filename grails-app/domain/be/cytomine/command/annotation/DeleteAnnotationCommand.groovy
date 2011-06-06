package be.cytomine.command.annotation

import be.cytomine.ontology.Annotation
import grails.converters.JSON

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException

class DeleteAnnotationCommand extends DeleteCommand implements UndoRedoCommand {

  boolean saveOnUndoRedoStack = true;

  String toString() {"DeleteAnnotationCommand"}

  def execute() {
    log.info "Execute"
    try {
      def postData = JSON.parse(postData)
      Annotation annotation = Annotation.findById(postData.id)
      return super.deleteAndCreateDeleteMessage(postData.id, annotation, "Annotation", [annotation.id, annotation.imageFileName()] as Object[])

    } catch (NullPointerException e) {
      log.error(e)
      return [data: [success: false, errors: e.getMessage()], status: 404]
    } catch (BackingStoreException e) {
      log.error(e)
      return [data: [success: false, errors: e.getMessage()], status: 400]
    }
  }

  def undo() {
    log.info("Undo")
    def annotationData = JSON.parse(data)
    Annotation annotation = Annotation.createFromData(annotationData)
    annotation.id = annotationData.id;
    annotation.save(flush: true)
    log.error "Annotation errors = " + annotation.errors

    return super.createUndoMessage(
            annotation,
            'Annotation',
            [annotation.id, annotation.imageFileName()] as Object[]
    );
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Annotation annotation = Annotation.findById(postData.id)
    String filename = annotation.imageFileName()
    annotation.delete(flush: true);
    String id = postData.id

    return super.createRedoMessage(
            id,
            'Annotation',
            [postData.id, filename] as Object[]
    );
  }

}
