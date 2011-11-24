package be.cytomine.command.imageinstance

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.ImageInstance
import grails.converters.JSON

class DeleteImageInstanceCommand extends DeleteCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() {
         //Retrieve domain
        ImageInstance domain = ImageInstance.get(json.id)
        if (!domain) throw new ObjectNotFoundException("Image instance $json.id not found!")
        //Build response message
        String message = createMessage(domain, [domain.id, domain?.baseImage?.filename, domain.project.name])
        //Init command info
        super.initCurrentCommantProject(domain.project)
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return restore(imageInstanceService,JSON.parse(data))
    }

    def redo() {
        return destroy(imageInstanceService,JSON.parse(data))
    }

}
