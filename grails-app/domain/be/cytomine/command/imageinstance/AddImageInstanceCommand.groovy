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
        return destroy(imageInstanceService,JSON.parse(data))
    }

    def redo() {
        return restore(imageInstanceService,JSON.parse(data))
    }
}
