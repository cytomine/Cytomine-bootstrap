package be.cytomine.command.group

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import be.cytomine.security.Group
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddGroupCommand extends AddCommand implements UndoRedoCommand {

    static constraints = {
    }

    def execute() {
        log.info("Execute")
        Group newGroup=null
        try {
            def json = JSON.parse(postData)
            newGroup = Group.createFromData(json)
            return super.validateAndSave(newGroup,["#ID#",newGroup.name] as Object[])
            //errors:
        }catch(ConstraintException  ex){
            return [data : [group:newGroup,errors:newGroup.retrieveErrors()], status : 400]
        }catch(IllegalArgumentException ex){
            return [data : [user:null,errors:["Cannot save group:"+ex.toString()]], status : 400]
        }
    }

    def undo() {
        log.info("Undo")
        def groupData = JSON.parse(data)
        def group = Group.findById(groupData.id)
        log.debug("Delete group with id:"+groupData.id)
        group.delete(flush:true)
        String id = groupData.id
        return super.createUndoMessage(id,user,[groupData.id,groupData.name] as Object[]);
    }

    def redo() {
        log.info("Undo")
        def groupData = JSON.parse(data)
        def group = Group.createFromData(groupData)
        group.id = groupData.id
        group.save(flush:true)
        return super.createRedoMessage(user,[groupData.id,groupData.name] as Object[]);
    }


}
