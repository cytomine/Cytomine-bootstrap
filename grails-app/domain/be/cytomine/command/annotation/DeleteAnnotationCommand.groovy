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
      super.changeCurrentProject(annotation.image.project)
      return super.deleteAndCreateDeleteMessage(postData.id, annotation, [annotation.id, annotation.imageFileName()] as Object[])

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
    def callback = [annotationID : annotation.id , imageID : annotation.image.id ]
    return super.createUndoMessage(annotation,[annotation.id, annotation.imageFileName()] as Object[],callback);
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Annotation annotation = Annotation.findById(postData.id)
    String filename = annotation.imageFileName()
    String idImage = annotation.image.id
    annotation.delete(flush: true);
    String id = postData.id
    def callback = [annotationID : id , imageID : idImage ]
    return super.createRedoMessage(id,annotation,[postData.id, filename] as Object[],callback);
  }

}
