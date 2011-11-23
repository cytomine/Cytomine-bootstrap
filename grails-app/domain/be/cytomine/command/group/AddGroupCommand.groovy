package be.cytomine.command.group

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.security.Group

class AddGroupCommand extends AddCommand implements SimpleCommand {

    def execute() {
        Group newGroup = Group.createFromData(json)
        return super.validateAndSave(newGroup, ["#ID#", newGroup.name] as Object[])
    }
}
