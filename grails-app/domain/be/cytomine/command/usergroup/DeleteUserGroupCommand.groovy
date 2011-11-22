package be.cytomine.command.usergroup

import be.cytomine.security.UserGroup
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.command.SimpleCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.Exception.ObjectNotFoundException


class DeleteUserGroupCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        User user = User.read(json.user)
        Group group = Group.read(json.group)
        UserGroup userGroup = UserGroup.findByUserAndGroup(user,  group)
        if(!userGroup) throw new ObjectNotFoundException("Usergroup with user $user and group $group not found")
        return super.deleteAndCreateDeleteMessage(user.id, userGroup, [userGroup.user, userGroup.group] as Object[])
    }
}