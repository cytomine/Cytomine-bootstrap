package be.cytomine.command.secusersecrole

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.SecUserSecRole

class AddSecUserSecRoleCommand extends AddCommand implements SimpleCommand {

    def domainService

    def execute() {
        //Init new domain object
        SecUserSecRole newRelation = SecUserSecRole.createFromData(json)
        domainService.saveDomain(newRelation)
        //Build response message
        String message = createMessage(newRelation,[newRelation.id, newRelation.secUser])
        //Init command info
        fillCommandInfo(newRelation,message)
        //Create and return response
        return responseService.createResponseMessage(newRelation,message,printMessage)
    }

}
