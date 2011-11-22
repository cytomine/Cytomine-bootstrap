package be.cytomine.command.group

import be.cytomine.security.Group
import be.cytomine.command.DeleteCommand
import be.cytomine.command.SimpleCommand

class DeleteGroupCommand extends DeleteCommand implements SimpleCommand {

    def execute() {
        Group group = Group.findById(json.id)
        return super.deleteAndCreateDeleteMessage(json.id, group, [group.id, group.name] as Object[])
    }
}
