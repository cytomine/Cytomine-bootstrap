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
        //Retrieve domain
        Annotation updatedDomain = Annotation.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Annotation ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain?.image?.baseImage?.filename])
        //Init command info
        super.initCurrentCommantProject(updatedDomain.image.project)
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
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
