package be.cytomine.command.usergroup

import be.cytomine.security.UserGroup
import be.cytomine.command.SimpleCommand
import be.cytomine.command.AddCommand

class AddUserGroupCommand extends AddCommand implements SimpleCommand {

    def execute() {
        UserGroup userGroup = UserGroup.createFromData(json)
        return super.validateAndSave(userGroup, ["#ID#", userGroup.user, userGroup.group] as Object[])
    }

}
