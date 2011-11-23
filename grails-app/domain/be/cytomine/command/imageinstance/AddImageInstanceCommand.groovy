package be.cytomine.command.imageinstance

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 */
class AddImageInstanceCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        json.user = user.id
        ImageInstance newImage = ImageInstance.createFromData(json)
        newImage.slide = newImage.baseImage.slide
        super.changeCurrentProject(newImage.project)

        def oldImageInstance = ImageInstance.findByBaseImageAndProject(newImage.baseImage, newImage.project)
        if (oldImageInstance) throw new WrongArgumentException("Image " + newImage?.baseImage?.filename + " already map with project")

        return super.validateAndSave(newImage, ["#ID#", newImage?.baseImage?.filename, newImage.project.name] as Object[])
    }


    def undo() {
        log.info("Undo")
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.get(imageData.id)
        image.delete(flush: true)
        String id = imageData.id
        return super.createUndoMessage(id, image, [imageData.id, AbstractImage.read(imageData.baseImage).filename, Project.read(imageData.project)] as Object[]);
    }


    def redo() {
        log.info("Redo")
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.createFromData(imageData)
        image.id = imageData.id
        image.save(flush: true)
        return super.createRedoMessage(image, [imageData.id, image?.baseImage?.filename, image.project.name] as Object[]);
    }
}
