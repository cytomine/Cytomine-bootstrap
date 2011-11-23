package be.cytomine.command.imageinstance

import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.ImageInstance
import grails.converters.JSON

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
        ImageInstance updatedImage = ImageInstance.get(json.id)
        super.changeCurrentProject(updatedImage.project)
        return super.validateAndSave(json, updatedImage, [updatedImage.id, updatedImage?.baseImage?.filename, updatedImage.project.name] as Object[])
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
