package be.cytomine.command.abstractimagegroup

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.security.Group
import grails.converters.JSON

class DeleteAbstractImageGroupCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute()  {
        //Retrieve domain
        AbstractImage abstractimage = AbstractImage.get(json.abstractimage)
        Group group = Group.get(json.group)
        AbstractImageGroup relation = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
        if (!relation) throw new ObjectNotFoundException("AbstractImageGroup not found " + abstractimage + " group=" + group)
        //Build response message
        String message = createMessage(relation, [relation.id, relation.abstractimage.id, relation.group.name])
        //Init command info
        fillCommandInfo(relation,message)
        //Delete domain
        AbstractImageGroup.unlink(relation.abstractimage, relation.group)
        //Create and return response
        return responseService.createResponseMessage(relation,message,printMessage)
    }

    def undo() {
        return restore(abstractImageGroupService,JSON.parse(data))
    }

    def redo() {
        return destroy(abstractImageGroupService,JSON.parse(data))
    }

}