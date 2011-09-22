package be.cytomine.command.group

import grails.converters.JSON
import be.cytomine.security.Group
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.command.EditCommand
import be.cytomine.command.SimpleCommand

class EditGroupCommand extends EditCommand implements SimpleCommand {

    def execute() {
        log.info "Execute"
        Group updatedGroup=null
        try {
            def postData = JSON.parse(postData)
            updatedGroup = Group.get(postData.id)
            return super.validateAndSave(postData,updatedGroup,[updatedGroup.id,updatedGroup.name] as Object[])
        } catch(NullPointerException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 404]
        } catch(ConstraintException e) {
            log.error(e)
            return [data : [success : false, errors : updatedGroup.retrieveErrors()], status : 400]
        } catch(IllegalArgumentException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 400]
        }
    }
}
