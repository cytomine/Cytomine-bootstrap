package be.cytomine.command.usergroup

import be.cytomine.security.UserGroup
import be.cytomine.security.Group
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.command.SimpleCommand
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException

class DeleteUserGroupCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        try {
            def postData = JSON.parse(postData)
            User user = User.read(postData.user)
            Group group = Group.read(postData.group)
            UserGroup userGroup = UserGroup.findByUserAndGroup(user, group)
            return super.deleteAndCreateDeleteMessage(user.id,userGroup,[userGroup.user,userGroup.group] as Object[])
        } catch(NullPointerException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 404]
        } catch(BackingStoreException e) {
            log.error(e)
            return [data : [success : false, errors : e.getMessage()], status : 400]
        }
    }
}