package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

class RestSecUserSecRoleController extends RestController {

    def userService
    def secRoleService
    def secUserSecRoleService
    def cytomineService
    def transactionService

    @Secured(['ROLE_ADMIN'])
    def list = {
        User user = userService.read(params.long('user'));
        responseSuccess(secUserSecRoleService.list(user))
    }

    @Secured(['ROLE_ADMIN'])
    def show = {
        User user = userService.read(params.long('user'));
        SecRole role = secRoleService.read(params.long('role'));
        SecUserSecRole secUserSecRole = secUserSecRoleService.get(user, role)
        if (!secUserSecRole) responseNotFound("SecUserSecRole", params.user)
        responseSuccess(secUserSecRole)
    }

    @Secured(['ROLE_ADMIN'])
    def save = {
        add(secUserSecRoleService, request.JSON)
    }

    @Secured(['ROLE_ADMIN'])
    def delete = {
        delete(secUserSecRoleService, JSON.parse("{user : $params.user, role: $params.role}"))
    }

    @Secured(['ROLE_ADMIN'])
    def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder = params.sord ?: 'asc'
        def maxRows = 50//params.row ? Integer.valueOf(params.rows) : 20
        def currentPage = params.page ? Integer.valueOf(params.page) : 1
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

        def secRoles = secUserSecRoleService.list(sortIndex, sortOrder, maxRows, currentPage, rowOffset)

        def totalRows = secRoles.totalCount
        def numberOfPages = Math.ceil(totalRows / maxRows)
        def jsonData = [rows: secRoles, page: currentPage, records: totalRows, total: numberOfPages]
        render jsonData as JSON
    }

}