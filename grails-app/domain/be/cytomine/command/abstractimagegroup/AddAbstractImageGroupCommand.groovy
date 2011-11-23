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

    def execute() throws CytomineException {
        //Init new domain object
        AbstractImageGroup newAbstractImageGroup = AbstractImageGroup.createAbstractImageGroupFromData(json)
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
        def abstractimageGroupData = JSON.parse(data)
        def abstractimage = AbstractImage.get(abstractimageGroupData.abstractimage)
        def group = Group.get(abstractimageGroupData.group)
        def abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)

        AbstractImageGroup.unlink(abstractimageGroup.abstractimage, abstractimageGroup.group)

        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("abstractimageID", abstractimage.id)
        callback.put("groupID", group.id)
        callback.put("imageID", abstractimage.id)

        log.debug "AbstractImageGroup=" + abstractimageGroupData.id + " abstractimage.filename=" + abstractimage.filename + " group.name=" + group.name
        String id = abstractimageGroupData.id
        return super.createUndoMessage(id, abstractimageGroup, [id, abstractimage.filename, group.name] as Object[], callback);
    }



    def redo() {
        def abstractimageGroupData = JSON.parse(data)

        def abstractimage = AbstractImage.get(abstractimageGroupData.abstractimage)
        def group = Group.get(abstractimageGroupData.group)

        def abstractimageGroup = AbstractImageGroup.createAbstractImageGroupFromData(abstractimageGroupData)

        AbstractImageGroup.link(abstractimage, group)

        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("abstractimageID", abstractimage.id)
        callback.put("groupID", group.id)
        callback.put("imageID", abstractimage.id)

        return super.createRedoMessage(abstractimageGroup, [id, abstractimage.filename, group.name] as Object[], callback);
    }

}
