package be.cytomine.api

import be.cytomine.security.UserGroup
import be.cytomine.security.User
import grails.plugins.springsecurity.Secured
import be.cytomine.security.Group
import be.cytomine.command.Command
import grails.converters.JSON
import be.cytomine.command.usergroup.AddUserGroupCommand
import be.cytomine.command.usergroup.DeleteUserGroupCommand

class RestUserGroupController extends RestController {

    @Secured(['ROLE_ADMIN'])
    def list = {
        User user = User.read(params.user);
        responseSuccess(UserGroup.findAllByUser(user))
    }

 	@Secured(['ROLE_ADMIN'])
    def show = {
		User user = User.read(params.user);
		Group group = Group.read(params.group);
        UserGroup userGroup = UserGroup.findByUserAndGroup(user, group)
        if (!userGroup) responseNotFound("UserGroup", params.user)
        responseSuccess(userGroup)
    }

    @Secured(['ROLE_ADMIN'])
    def save = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand( new AddUserGroupCommand(user: currentUser), request.JSON)
        response(result)
    }

    @Secured(['ROLE_ADMIN'])
    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = JSON.parse("{user: $params.user, group : $params.group}")
        def result = processCommand(new DeleteUserGroupCommand(postData: postData.toString(), user: currentUser), json)
        response(result)
    }
}
