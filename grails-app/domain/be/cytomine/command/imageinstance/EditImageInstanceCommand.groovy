package be.cytomine.command.imageinstance

import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import be.cytomine.Exception.ObjectNotFoundException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
class EditImageInstanceCommand extends EditCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve domain
        ImageInstance updatedDomain = ImageInstance.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("ImageInstance ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain,json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain?.baseImage?.filename, updatedDomain.project.name])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        super.initCurrentCommantProject(updatedDomain.project)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
    }

    def undo() {
        log.info "Undo"
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.findById(imageData.previousImageInstance.id)
        image = ImageInstance.getFromData(image, imageData.previousImageInstance)
        image.save(flush: true)
        super.createUndoMessage(imageData, image, [image.id, image?.baseImage?.filename, image.project.name] as Object[])
    }

    def redo() {
        log.info "Redo"
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.findById(imageData.newImageInstance.id)
        image = ImageInstance.getFromData(image, imageData.newImageInstance)
        image.save(flush: true)
        super.createRedoMessage(imageData, image, [image.id, image?.baseImage?.filename, image.project.name] as Object[])
    }
}
