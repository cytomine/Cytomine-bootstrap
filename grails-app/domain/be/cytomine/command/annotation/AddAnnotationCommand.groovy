package be.cytomine.command.annotation

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import grails.converters.JSON

class AddAnnotationCommand extends AddCommand implements UndoRedoCommand {

    def domainService

    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        json.user = user.id
        Annotation newAnnotation = Annotation.createFromData(json)

        if (!newAnnotation.location) throw new WrongArgumentException("Geo is null: 0 points")
        if (newAnnotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geo is empty:" + newAnnotation.location.getNumPoints() + " points")

        domainService.saveDomain(newAnnotation)
        //Build response message
        String message = createMessage(newAnnotation, [newAnnotation.id, newAnnotation?.imageFileName()])
        //Init command info
        fillCommandInfo(newAnnotation, message)
        super.initCurrentCommantProject(newAnnotation?.image?.project)
        //Create and return response
        return responseService.createResponseMessage(newAnnotation, message, printMessage)
    }

    def undo() {
        def annotationData = JSON.parse(data)
        String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
        Annotation annotation = Annotation.get(annotationData.id)
        def callback = [annotationID: annotation.id, imageID: annotation.image.id]
        annotation.delete(flush: true)
        String id = annotationData.id
        return super.createUndoMessage(id, annotation, [annotationData.id, filename] as Object[], callback);
    }

    def redo() {
        def annotationData = JSON.parse(data)
        String filename = ImageInstance.get(annotationData.image)?.baseImage?.filename
        def annotation = Annotation.createFromData(annotationData)
        def callback = [annotationID: annotationData.id, imageID: annotation.image.id]
        annotation.id = annotationData.id
        annotation.save(flush: true)
        return super.createRedoMessage(annotation, [annotationData.id, filename] as Object[], callback);
    }
}
