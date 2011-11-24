package be.cytomine.command.annotation

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import grails.converters.JSON

class AddAnnotationCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute()  {
        //Create new domain
        Annotation newAnnotation = Annotation.createFromData(json)
        if (!newAnnotation.location) throw new WrongArgumentException("Geo is null: 0 points")
        if (newAnnotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geo is empty:" + newAnnotation.location.getNumPoints() + " points")
        //Validate and save domain
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
        return destroy(annotationService,JSON.parse(data))
    }

    def redo() {
        return restore(annotationService,JSON.parse(data))
    }
}