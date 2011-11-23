package be.cytomine.command.annotation

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import grails.converters.JSON

class EditAnnotationCommand extends EditCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        try {
            json.user = user.id
            Annotation updatedAnnotation = Annotation.get(json.id)
            if (!updatedAnnotation) throw new ObjectNotFoundException("Annotation " + json.id + " not found")
            String filename = updatedAnnotation.image?.baseImage?.getFilename()
            super.initCurrentCommantProject(updatedAnnotation.image.project)
            return super.validateAndSave(json, updatedAnnotation, [updatedAnnotation.id, filename] as Object[])
        } catch (com.vividsolutions.jts.io.ParseException e) {
            log.error "New annotation can't be saved (bad geom): " + e.toString()
            throw new WrongArgumentException("Location invalid:" + json.location)
        }

    }

    def undo() {
        log.info "Undo"
        def annotationsData = JSON.parse(data)
        Annotation annotation = Annotation.findById(annotationsData.previousAnnotation.id)
        annotation = Annotation.getFromData(annotation, annotationsData.previousAnnotation)
        annotation.save(flush: true)
        def filename = annotation.image?.baseImage?.getFilename()
        def callback = [annotationID: annotation.id, imageID: annotation.image.id]
        super.createUndoMessage(annotationsData, annotation, [annotation.id, filename] as Object[], callback)
    }


    def redo() {
        log.info "Redo"
        def annotationsData = JSON.parse(data)
        Annotation annotation = Annotation.findById(annotationsData.newAnnotation.id)
        annotation = Annotation.getFromData(annotation, annotationsData.newAnnotation)
        annotation.save(flush: true)
        def filename = annotation.image?.baseImage?.getFilename()
        def callback = [annotationID: annotation.id, imageID: annotation.image.id]
        super.createRedoMessage(annotationsData, annotation, [annotation.id, filename] as Object[], callback)
    }

}
