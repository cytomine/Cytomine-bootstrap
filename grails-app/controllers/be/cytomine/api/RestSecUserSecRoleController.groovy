package be.cytomine.api

import grails.plugins.springsecurity.Secured
import be.cytomine.security.SecUserSecRole
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.secusersecrole.AddSecUserSecRoleCommand
import be.cytomine.command.secusersecrole.DeleteSecUserSecRoleCommand
import be.cytomine.command.secusersecrole.EditSecUserSecRoleCommand
import grails.converters.JSON
import be.cytomine.security.SecRole

class RestSecUserSecRoleController extends RestController {

    @Secured(['ROLE_ADMIN'])
    def list = {
        User user = User.read(params.user);
        responseSuccess(SecUserSecRole.findAllBySecUser(user))
    }

 	@Secured(['ROLE_ADMIN'])
    def show = {
		User user = User.read(params.user);
		SecRole role = SecRole.read(params.role);
        SecUserSecRole secUserSecRole = SecUserSecRole.findBySecUserAndSecRole(user, role)
        if (!secUserSecRole) responseNotFound("SecUserSecRole", params.user)
        responseSuccess(secUserSecRole)
    }

    @Secured(['ROLE_ADMIN'])
    def save = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        Command addSecUserSecRoleCommand = new AddSecUserSecRoleCommand(postData: request.JSON.toString(), user: currentUser)
        def result = processCommand(addSecUserSecRoleCommand, currentUser)
        response(result)
    }

    @Secured(['ROLE_ADMIN'])
    def update = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        Command editSecUserSecRoleCommand = new EditSecUserSecRoleCommand(postData: request.JSON.toString(), user: currentUser)
        def result = processCommand(editSecUserSecRoleCommand, currentUser)
        response(result)
    }

    @Secured(['ROLE_ADMIN'])
    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def postData = ([user: params.user, role : params.role]) as JSON
        Command deleteSecUserSecRoleCommand = new DeleteSecUserSecRoleCommand(postData: postData.toString(), user: currentUser)
        def result = processCommand(deleteSecUserSecRoleCommand, currentUser)
        response(result)
    }

    @Secured(['ROLE_ADMIN'])
    def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder  = params.sord ?: 'asc'
        def maxRows = 50//params.row ? Integer.valueOf(params.rows) : 20
        def currentPage = params.page ? Integer.valueOf(params.page) : 1
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
        def secRoles = SecUserSecRole.createCriteria().list(max: maxRows, offset: rowOffset) {
            order(sortIndex, sortOrder).ignoreCase()
        }

        def totalRows = secRoles.totalCount
        def numberOfPages = Math.ceil(totalRows / maxRows)
        def jsonData = [rows: secRoles, page: currentPage, records: totalRows, total: numberOfPages]
        render jsonData as JSON
    }

}
