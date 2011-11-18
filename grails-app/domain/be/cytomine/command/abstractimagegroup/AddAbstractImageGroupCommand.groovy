package be.cytomine.command.abstractimagegroup

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand

import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.Exception.CytomineException

class AddAbstractImageGroupCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;

  def execute() throws CytomineException {
      def json = JSON.parse(postData)
      AbstractImageGroup newAbstractImageGroup = AbstractImageGroup.createAbstractImageGroupFromData(json)
      AbstractImageGroup.link(newAbstractImageGroup.abstractimage,newAbstractImageGroup.group)
      return super.validateWithoutSave(newAbstractImageGroup,["#ID#",newAbstractImageGroup.abstractimage.filename,newAbstractImageGroup.group.name] as Object[])
  }

  def undo() {
    log.info("Undo")
    def abstractimageGroupData = JSON.parse(data)
    def abstractimage = AbstractImage.get(abstractimageGroupData.abstractimage)
    def group = Group.get(abstractimageGroupData.group)
    def abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage,group)

    AbstractImageGroup.unlink(abstractimageGroup.abstractimage,abstractimageGroup.group)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("abstractimageID",abstractimage.id)
    callback.put("groupID",group.id)
    callback.put("imageID",abstractimage.id)

    log.debug "AbstractImageGroup=" + abstractimageGroupData.id +" abstractimage.filename=" + abstractimage.filename  + " group.name=" + group.name
    String id = abstractimageGroupData.id
    return super.createUndoMessage(id,abstractimageGroup,[id,abstractimage.filename,group.name] as Object[],callback);
  }



  def redo() {
    log.info("Redo")
    def abstractimageGroupData = JSON.parse(data)

    def abstractimage = AbstractImage.get(abstractimageGroupData.abstractimage)
    def group = Group.get(abstractimageGroupData.group)

    def abstractimageGroup = AbstractImageGroup.createAbstractImageGroupFromData(abstractimageGroupData)

    AbstractImageGroup.link(abstractimage,group)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("abstractimageID",abstractimage.id)
    callback.put("groupID",group.id)
    callback.put("imageID",abstractimage.id)

    return super.createRedoMessage( abstractimageGroup,[id,abstractimage.filename,group.name] as Object[],callback);
  }

}
