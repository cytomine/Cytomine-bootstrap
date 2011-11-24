package be.cytomine.command.abstractimage

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 */
class AddAbstractImageCommand extends AddCommand implements UndoRedoCommand {

    def execute() {
        json.user = user.id
        //Init new domain object
        AbstractImage newImage = AbstractImage.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(newImage)
        //Build response message
        String message = createMessage(newImage, [newImage.id, newImage.filename])
        //Init command info
        fillCommandInfo(newImage,message)
        //Create and return response
        return responseService.createResponseMessage(newImage,message,printMessage)
    }

    def undo() {
        return destroy(abstractImageService,JSON.parse(data))
    }

    def redo() {
        return restore(abstractImageService,JSON.parse(data))
    }
}
