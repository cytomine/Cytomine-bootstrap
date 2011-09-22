package be.cytomine.command.secusersecrole

import be.cytomine.command.UndoRedoCommand
import be.cytomine.command.AddCommand
import be.cytomine.security.SecUserSecRole
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.command.SimpleCommand

class AddSecUserSecRoleCommand extends AddCommand implements SimpleCommand {

    def execute() {
        SecUserSecRole userRole = null
        try {
            def json = JSON.parse(postData)
            userRole = SecUserSecRole.createFromData(json)
            return super.validateAndSave(userRole,["#ID#",userRole.secUser] as Object[])
            //errors:
        }catch(ConstraintException  ex){
            return [data : [role:userRole,errors:userRole.retrieveErrors()], status : 400]
        }catch(IllegalArgumentException ex){
            return [data : [user:null,errors:["Cannot save userRole:"+ex.toString()]], status : 400]
        }
    }

}
