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
        //Init new domain object
        ImageInstance domain = ImageInstance.createFromData(json)
        def alreadyExistImage = ImageInstance.findByBaseImageAndProject(domain.baseImage, domain.project)
        if (alreadyExistImage) throw new WrongArgumentException("Image " + domain?.baseImage?.filename + " already map with project")
        domain.slide = domain.baseImage.slide
        initCurrentCommantProject(domain.project)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain?.baseImage?.filename, domain.project.name])
        //Init command info
        fillCommandInfo(domain, message)
        //Create and return response
        return responseService.createResponseMessage(domain, message, printMessage)
    }


    def undo() {
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.get(imageData.id)
        image.delete(flush: true)
        String id = imageData.id
        return super.createUndoMessage(id, image, [imageData.id, AbstractImage.read(imageData.baseImage).filename, Project.read(imageData.project)] as Object[]);
    }


    def redo() {
        def imageData = JSON.parse(data)
        ImageInstance image = ImageInstance.createFromData(imageData)
        image.id = imageData.id
        image.save(flush: true)
        return super.createRedoMessage(image, [imageData.id, image?.baseImage?.filename, image.project.name] as Object[]);
    }
}
