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

    def execute() throws CytomineException {
        json.user = user.id
        AbstractImage newImage = AbstractImage.createFromData(json)
        return super.validateAndSave(newImage, ["#ID#", json.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.get(imageData.id)
        image.delete(flush: true)
        String id = imageData.id
        return super.createUndoMessage(id, [imageData.id, imageData.name] as Object[]);
    }

    def redo() {
        log.info("Redo")
        def imageData = JSON.parse(data)
        AbstractImage image = AbstractImage.createFromData(imageData)
        image.id = imageData.id
        image.save(flush: true)
        return super.createRedoMessage(image, [imageData.id, imageData.name] as Object[]);
    }
}
