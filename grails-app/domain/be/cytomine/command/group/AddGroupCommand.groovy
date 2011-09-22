package be.cytomine.command.group

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import be.cytomine.security.Group
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.command.SimpleCommand

class AddGroupCommand extends AddCommand implements SimpleCommand {

    def execute() {
        log.info("Execute")
        Group newGroup=null
        try {
            def json = JSON.parse(postData)
            newGroup = Group.createFromData(json)
            return super.validateAndSave(newGroup,["#ID#",newGroup.name] as Object[])
        }catch(ConstraintException  ex){
            return [data : [group:newGroup,errors:newGroup.retrieveErrors()], status : 400]
        }catch(IllegalArgumentException ex){
            return [data : [user:null,errors:["Cannot save group:"+ex.toString()]], status : 400]
        }
    }

}
