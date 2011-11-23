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
        AbstractImage updatedImage = AbstractImage.get(json.id)
        log.info "### before validateAndSave"
        if (!updatedImage) throw new ObjectNotFoundException("Image " + json.id + " was not found")
        def data = super.validateAndSave(json, updatedImage, [updatedImage.id, updatedImage.filename] as Object[])
        log.info "### after validateAndSave"
        return data
    }

    def undo() {
        log.info "Undo"
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.findById(imageData.previousImage.id)
        image = AbstractImage.getFromData(image, imageData.previousImage)
        image.save(flush: true)
        super.createUndoMessage(imageData, image, [image.id, image.filename] as Object[])
    }

    def redo() {
        log.info "Redo"
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.findById(imageData.newImage.id)
        image = AbstractImage.getFromData(image, imageData.newImage)
        image.save(flush: true)
        super.createRedoMessage(imageData, image, [image.id, image.filename] as Object[])
    }

}
