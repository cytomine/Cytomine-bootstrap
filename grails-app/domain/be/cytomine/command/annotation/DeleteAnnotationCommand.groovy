package be.cytomine.command.annotation

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import grails.converters.JSON

class DeleteAnnotationCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    String toString() {"DeleteAnnotationCommand"}

    def execute() throws CytomineException {
        Annotation annotation = Annotation.findById(json.id)
        if (!annotation) throw new ObjectNotFoundException("Annotation " + json.id + " not found")
        super.changeCurrentProject(annotation.image.project)
        return super.deleteAndCreateDeleteMessage(json.id, annotation, [annotation.id, annotation.imageFileName()] as Object[])
    }

    def undo() {
        log.info("Undo")
        def annotationData = JSON.parse(data)
        Annotation annotation = Annotation.createFromData(annotationData)
        annotation.id = annotationData.id;
        annotation.save(flush: true)
        def callback = [annotationID: annotation.id, imageID: annotation.image.id]
        return super.createUndoMessage(annotation, [annotation.id, annotation.imageFileName()] as Object[], callback);
    }

    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        Annotation annotation = Annotation.findById(postData.id)
        String filename = annotation.imageFileName()
        String idImage = annotation.image.id
        annotation.delete(flush: true);
        String id = postData.id
        def callback = [annotationID: id, imageID: idImage]
        return super.createRedoMessage(id, annotation, [postData.id, filename] as Object[], callback);
    }

}
