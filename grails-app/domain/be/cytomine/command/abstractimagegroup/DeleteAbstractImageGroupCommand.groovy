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
        log.info("Undo")
        def abstractimageGroupData = JSON.parse(data)
        def abstractimage = AbstractImage.get(abstractimageGroupData.abstractimage)
        def group = Group.get(abstractimageGroupData.group)

        AbstractImageGroup abstractimageGroup = AbstractImageGroup.createAbstractImageGroupFromData(abstractimageGroupData)
        abstractimageGroup = AbstractImageGroup.link(abstractimageGroupData.id, abstractimage, group)

        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("abstractimageID", abstractimage.id)
        callback.put("groupID", group.id)
        callback.put("imageID", abstractimage.id)

        return super.createUndoMessage(abstractimageGroup, [id, abstractimage.id, group.name] as Object[], callback
        );
    }



    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        AbstractImage abstractimage = AbstractImage.get(postData.abstractimage)
        Group group = Group.get(postData.group)

        AbstractImageGroup abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
        String id = abstractimageGroup.id
        AbstractImageGroup.unlink(abstractimageGroup.abstractimage, abstractimageGroup.group)

        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("abstractimageID", abstractimage.id)
        callback.put("groupID", group.id)
        callback.put("imageID", abstractimage.id)

        return super.createRedoMessage(id, abstractimageGroup, [id, abstractimage.id, group.name] as Object[], callback
        );
    }

}