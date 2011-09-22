package be.cytomine.command.abstractimagegroup

import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import grails.converters.JSON
import java.util.prefs.BackingStoreException
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.image.AbstractImageGroup

class DeleteAbstractImageGroupCommand extends DeleteCommand implements UndoRedoCommand {

  boolean saveOnUndoRedoStack = false;

  def execute() {
    log.info "Execute"

    try {
      def postData = JSON.parse(postData)
      AbstractImage abstractimage = AbstractImage.get(postData.abstractimage)
      Group group = Group.get(postData.group)

      log.info "Delete abstractimage-group with abstractimage=" + abstractimage + " group=" + group

      AbstractImageGroup abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage,group)
      String id = abstractimageGroup.id

      def response = super.createDeleteMessage(id,abstractimageGroup,[id,abstractimage.id,group.name] as Object[])
      AbstractImageGroup.unlink(abstractimageGroup.abstractimage, abstractimageGroup.group)

      return response

    } catch (NullPointerException e) {
      log.error(e)
      return [data: [success: false, errors: e.getMessage()], status: 404]
    } catch (BackingStoreException e) {
      log.error(e)
      return [data: [success: false, errors: e.getMessage()], status: 400]
    }
  }



  def undo() {
    log.info("Undo")
    def abstractimageGroupData = JSON.parse(data)
    def abstractimage = AbstractImage.get(abstractimageGroupData.abstractimage)
    def group = Group.get(abstractimageGroupData.group)

    AbstractImageGroup abstractimageGroup = AbstractImageGroup.createAbstractImageGroupFromData(abstractimageGroupData)
    abstractimageGroup = AbstractImageGroup.link(abstractimageGroupData.id,abstractimage, group)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("abstractimageID",abstractimage.id)
    callback.put("groupID",group.id)
    callback.put("imageID",abstractimage.id)

    return super.createUndoMessage(abstractimageGroup,[id,abstractimage.id,group.name] as Object[],callback
    );
  }



  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    AbstractImage abstractimage = AbstractImage.get(postData.abstractimage)
    Group group = Group.get(postData.group)

    AbstractImageGroup abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage,group)
    String id =  abstractimageGroup.id
    AbstractImageGroup.unlink(abstractimageGroup.abstractimage, abstractimageGroup.group)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("abstractimageID",abstractimage.id)
    callback.put("groupID",group.id)
    callback.put("imageID",abstractimage.id)

    return super.createRedoMessage(id,abstractimageGroup,[id,abstractimage.id,group.name] as Object[],callback
    );
  }

}