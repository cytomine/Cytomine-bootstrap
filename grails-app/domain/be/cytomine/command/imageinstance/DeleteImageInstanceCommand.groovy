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
        log.info "Execute" + json
        ImageInstance image = ImageInstance.get(json.id)
        if (!image) throw new ObjectNotFoundException("Image instance $json.id not found!")
        super.changeCurrentProject(image.project)
        return super.deleteAndCreateDeleteMessage(json.id, image, [image.id, image?.baseImage?.filename, image.project.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.createFromData(imageData)
        image.id = imageData.id;
        image.save(flush: true)
        log.error "Image errors = " + image.errors
        return super.createUndoMessage(image, [image.id, image?.baseImage, image?.project?.name] as Object[]);
    }


    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        ImageInstance image = ImageInstance.findById(postData.id)
        String id = image.id
        String filename = image?.baseImage?.filename
        String projectname = image.project.name
        image.delete(flush: true);
        return super.createRedoMessage(id, image, [id, filename, projectname] as Object[]);
    }

}
