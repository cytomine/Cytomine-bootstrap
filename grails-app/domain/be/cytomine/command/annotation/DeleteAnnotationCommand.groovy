package be.cytomine.command.annotation

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import grails.converters.JSON

class DeleteAnnotationCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute()  {
        //Retrieve domain
        Annotation domain = Annotation.findById(json.id)
        if (!domain) throw new ObjectNotFoundException("Annotation " + json.id + " not found")
        //Build response message
        String message = createMessage(domain, [domain.id, domain.imageFileName()])
        //Init command info
        super.initCurrentCommantProject(domain.image.project)
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return restore(annotationService,JSON.parse(data))
    }

    def redo() {
        return destroy(annotationService,JSON.parse(data))
    }

}
