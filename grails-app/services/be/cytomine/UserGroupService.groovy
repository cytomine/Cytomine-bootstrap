package be.cytomine

import be.cytomine.security.UserGroup
import be.cytomine.security.User
import be.cytomine.security.Group
import be.cytomine.command.usergroup.AddUserGroupCommand
import grails.converters.JSON
import be.cytomine.command.usergroup.DeleteUserGroupCommand

class UserGroupService {

    static transactional = true

    def cytomineService
    def commandService

    def list(User user) {
        UserGroup.findAllByUser(user)
    }

    def get(User user, Group group) {
        UserGroup.findByUserAndGroup(user, group)
    }

    def save(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand( new AddUserGroupCommand(user: currentUser), json)
    }

    def delete(def idUser, def idGroup) {
        User currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{user: $idUser, group : $idGroup}")
        def result = commandService.processCommand(new DeleteUserGroupCommand(postData: json.toString(), user: currentUser), json)
        return result
    }
}
