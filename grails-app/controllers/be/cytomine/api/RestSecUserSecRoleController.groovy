package be.cytomine.api

import grails.plugins.springsecurity.Secured
import be.cytomine.security.SecUserSecRole
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.secusersecrole.AddSecUserSecRoleCommand
import be.cytomine.command.secusersecrole.DeleteSecUserSecRoleCommand
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
        def result = processCommand(new AddSecUserSecRoleCommand(user: currentUser), request.JSON)
        response(result)
    }

    @Secured(['ROLE_ADMIN'])
    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = JSON.parse("{user: $params.user, role: $params.role}")
        def result = processCommand(new DeleteSecUserSecRoleCommand(user: currentUser), json)
        response(result)
    }

    @Secured(['ROLE_ADMIN'])
    def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder = params.sord ?: 'asc'
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
