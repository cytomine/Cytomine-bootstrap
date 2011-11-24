package be.cytomine.command.abstractimage

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class EditAbstractImageCommand extends EditCommand implements UndoRedoCommand {

    def execute() throws CytomineException {
        //Retrieve domain
        AbstractImage updatedDomain = AbstractImage.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Image ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain.filename])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
    }

    def undo() {
        return edit(abstractImageService,JSON.parse(data).previousImage)
    }

    def redo() {
        return edit(abstractImageService,JSON.parse(data).newImage)
    }
}
