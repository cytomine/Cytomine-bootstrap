package be.cytomine.command.usergroup

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.UserGroup

class AddUserGroupCommand extends AddCommand implements SimpleCommand {

    def execute() {
        UserGroup userGroup = UserGroup.createFromData(json)
        return super.validateAndSave(userGroup, ["#ID#", userGroup.user, userGroup.group] as Object[])
    }

}
