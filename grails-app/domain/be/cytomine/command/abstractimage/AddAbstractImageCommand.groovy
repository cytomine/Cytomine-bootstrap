package be.cytomine.command.abstractimage

import be.cytomine.Exception.CytomineException
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
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.get(imageData.id)
        image.delete(flush: true)
        String id = imageData.id
        return super.createUndoMessage(id, [imageData.id, imageData.name] as Object[]);
    }

    def redo() {
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.createFromData(imageData)
        image.id = imageData.id
        image.save(flush: true)
        return super.createRedoMessage(image, [imageData.id, imageData.name] as Object[]);
    }
}
