package be.cytomine.api

import be.cytomine.security.UserGroup
import be.cytomine.security.User
import grails.plugins.springsecurity.Secured
import be.cytomine.security.Group
import be.cytomine.command.Command
import grails.converters.JSON
import be.cytomine.command.usergroup.AddUserGroupCommand
import be.cytomine.command.usergroup.DeleteUserGroupCommand
import be.cytomine.Exception.CytomineException

class RestUserGroupController extends RestController {

    def userGroupService
    def userService
    def groupService
    def transactionService

    def cytomineService

    @Secured(['ROLE_ADMIN'])
    def list = {
        User user = userService.read(params.user);
        responseSuccess(userGroupService.list(user))
    }

 	@Secured(['ROLE_ADMIN'])
    def show = {
		User user = userService.read(params.user);
		Group group = groupService.read(params.group);
        UserGroup userGroup = userGroupService.get(user, group)
        if (!userGroup) responseNotFound("UserGroup", params.user)
        else responseSuccess(userGroup)
    }

    def add = {
        try {
            def result = userService.add(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def delete = {
        try {
            def result = userService.delete(params.id)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }
}
