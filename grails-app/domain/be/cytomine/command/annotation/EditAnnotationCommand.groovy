package be.cytomine.command.annotation

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
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
        return edit(annotationService,JSON.parse(data).previousAnnotation)
    }

    def redo() {
        return edit(annotationService,JSON.parse(data).newAnnotation)
    }

}
