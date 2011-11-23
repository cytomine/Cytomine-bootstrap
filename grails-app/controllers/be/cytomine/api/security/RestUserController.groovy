package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
class RestUserController extends RestController {

    def springSecurityService
    def transactionService

    def userService
    def projectService

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(userService.list())
    }

    /**
     * Render and return an User into the specified format given in the request
     * @param id the user identifier
     * @return user an User into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        User user = userService.read(params.id)
        if (user) responseSuccess(user)
        else responseNotFound("User", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCurrent = {
        responseSuccess(userService.readCurrentUser())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showByProject = {
        Project project = projectService.read(params.id)
        if (project) responseSuccess(project.users())
        else responseNotFound("User", "Project", params.id)
    }

    @Secured(['ROLE_ADMIN'])
    def add = {
        add(userService, request.JSON)
    }
    @Secured(['ROLE_ADMIN'])
    def update = {
        update(userService, request.JSON)
    }
    @Secured(['ROLE_ADMIN'])
    def delete = {
        delete(userService, JSON.parse("{id : $params.id}"))
    }


    @Secured(['ROLE_ADMIN'])
    def deleteUser = {
        Project project = Project.get(params.id)
        User user = User.get(params.idUser)

        synchronized (this.getClass()) {
            userService.deleteUserFromProject(user, project)
        }

        response.status = 201
        def ret = [data: [message: "OK"], status: 201]
        response(ret)
    }

    @Secured(['ROLE_ADMIN'])
    def addUser = {
        Project project = Project.get(params.id)
        User user = User.get(params.idUser)
        log.info "project= " + project

        synchronized (this.getClass()) {
            userService.addUserFromProject(user, project)
        }
        response.status = 201
        def ret = [data: [message: "OK"], status: 201]
        response(ret)

    }

    @Secured(['ROLE_ADMIN'])
    def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder = params.sord ?: 'asc'
        def maxRows = 50//params.row ? Integer.valueOf(params.rows) : 20
        def currentPage = params.page ? Integer.valueOf(params.page) : 1

        def users = userService.list(currentPage, maxRows, sortIndex, sortOrder, params.firstName, params.lastName, params.email)

        def totalRows = users.totalCount
        def numberOfPages = Math.ceil(totalRows / maxRows)
        def jsonData = [rows: users, page: currentPage, records: totalRows, total: numberOfPages]
        render jsonData as JSON
    }

}
