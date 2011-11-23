package be.cytomine.command.abstractimage

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import grails.converters.JSON

class DeleteAbstractImageCommand extends DeleteCommand implements UndoRedoCommand {

    def execute()  {
        //Retrieve domain
        AbstractImage domain = AbstractImage.findById(json.id)
        if (!domain) throw new ObjectNotFoundException("Image " + json.id + " was not found")
        //Build response message
        String message = createMessage(domain, [domain.id, domain.filename])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        log.info("Undo")
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.createFromData(imageData)
        image.id = imageData.id;
        image.save(flush: true)
        return super.createUndoMessage(image, [image.id, annotation.imageFileName()] as Object[]);
    }


    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        AbstractImage image = AbstractImage.findById(postData.id)
        String id = postData.id
        String filename = image.filename
        image.delete(flush: true);
        return super.createRedoMessage(id, 'Image', [postData.id, filename] as Object[]);
    }
}
