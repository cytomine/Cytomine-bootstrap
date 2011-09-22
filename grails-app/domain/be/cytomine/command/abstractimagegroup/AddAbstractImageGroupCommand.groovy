package be.cytomine.command.abstractimagegroup

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand

import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group

class AddAbstractImageGroupCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = false;
  def execute() {
    log.info("Execute")
    AbstractImageGroup newAbstractImageGroup=null
    try {
      def json = JSON.parse(postData)
      newAbstractImageGroup = AbstractImageGroup.createAbstractImageGroupFromData(json)
      AbstractImageGroup.link(newAbstractImageGroup.abstractimage,newAbstractImageGroup.group)
        return super.validateWithoutSave(newAbstractImageGroup,["#ID#",newAbstractImageGroup.abstractimage.filename,newAbstractImageGroup.group.name] as Object[])
      }catch(ConstraintException  ex){
      return [data : [abstractimagegroup:newAbstractImageGroup,errors:newAbstractImageGroup.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [abstractimagegroup:null,errors:["Cannot save abstractimage-group:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    log.info("data="+data)
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
    log.info("Redo="+data)
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
