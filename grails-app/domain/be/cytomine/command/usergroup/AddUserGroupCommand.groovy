package be.cytomine.command.usergroup

import be.cytomine.security.UserGroup
import be.cytomine.command.SimpleCommand
import be.cytomine.command.AddCommand
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddUserGroupCommand extends AddCommand implements SimpleCommand {

    def execute() {
        UserGroup userGroup = null
        try {
            def json = JSON.parse(postData)
            userGroup = UserGroup.createFromData(json)
            return super.validateAndSave(userGroup,["#ID#",userGroup.user, userGroup.group] as Object[])
            //errors:
        }catch(ConstraintException  ex){
            return [data : [role:userGroup,errors:userGroup.retrieveErrors()], status : 400]
        }catch(IllegalArgumentException ex){
            return [data : [user:null,errors:["Cannot save userGroup:"+ex.toString()]], status : 400]
        }
    }

}
