package be.cytomine.command.abstractimagegroup

import be.cytomine.Exception.CytomineException
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.security.Group
import grails.converters.JSON

class AddAbstractImageGroupCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Init new domain object
        AbstractImageGroup newAbstractImageGroup = AbstractImageGroup.createFromData(json)
        //Link relation domain
        newAbstractImageGroup = AbstractImageGroup.link(newAbstractImageGroup.abstractimage, newAbstractImageGroup.group)
        //Build response message
        String message = createMessage(newAbstractImageGroup, [newAbstractImageGroup.id, newAbstractImageGroup.abstractimage.filename, newAbstractImageGroup.group.name])
        //Init command info
        fillCommandInfo(newAbstractImageGroup,message)
        //Create and return response
        return responseService.createResponseMessage(newAbstractImageGroup,message,printMessage)
    }

    def undo() {
        return destroy(abstractImageGroupService,JSON.parse(data))
    }

    def redo() {
        return restore(abstractImageGroupService,JSON.parse(data))
    }

}
