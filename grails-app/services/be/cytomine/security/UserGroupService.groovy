package be.cytomine.security

import be.cytomine.ModelService
import be.cytomine.command.usergroup.AddUserGroupCommand
import be.cytomine.command.usergroup.DeleteUserGroupCommand

class UserGroupService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService

    def list(User user) {
        UserGroup.findAllByUser(user)
    }

    def get(User user, Group group) {
        UserGroup.findByUserAndGroup(user, group)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddUserGroupCommand(user: currentUser), json)
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new DeleteUserGroupCommand(postData: json.toString(), user: currentUser), json)
        return result
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
