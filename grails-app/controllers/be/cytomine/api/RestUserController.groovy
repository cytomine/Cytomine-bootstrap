package be.cytomine.api

import be.cytomine.security.User
import grails.converters.*
import be.cytomine.command.user.AddUserCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.command.Command
import be.cytomine.command.user.EditUserCommand
import be.cytomine.command.user.DeleteUserCommand
import be.cytomine.project.Project
import be.cytomine.api.RestController
import be.cytomine.security.Group
import be.cytomine.project.ProjectGroup
import be.cytomine.security.UserGroup
import grails.plugins.springsecurity.Secured

/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
class RestUserController extends RestController {

    def springSecurityService
    def transactionService

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(User.list(sort:"username", order:"asc"))
    }

    /**
     * Render and return an User into the specified format given in the request
     * @param id the user identifier
     * @return user an User into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        User user = User.read(params.id)
        if (user) responseSuccess(user)
        else responseNotFound("User", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCurrent = {
        responseSuccess(User.read(springSecurityService.principal.id))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showByProject = {
        Project project = Project.read(params.id)
        if (project)
            responseSuccess(project.users())
        else responseNotFound("User", "Project", params.id)
    }

    /**
     * Create a new User according to the parameters passed into the request.
     * If successful, the new user is rendered and returned into the specified format
     * given in the request. If not, validations errors messages are returned as a response.
     * @param data the data related to the new user
     * @return user the new User into the specified format
     */
    @Secured(['ROLE_ADMIN'])
    def save = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand( new AddUserCommand(user: currentUser), request.JSON)
        response(result)
    }

    /**
     * Edit an existing User according to the parameters passed into the request.
     * If successful, the user is rendered with its modifications and returned into the specified format
     * given in the request. If not, validations errors messages are returned as a response.
     * @param data the data related to the user
     * @return user the edited User into the specified format
     */
    @Secured(['ROLE_ADMIN'])
    def update = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditUserCommand(user: currentUser), request.JSON)
        response(result)
    }

    /**
     * Delete a user according to the identifier passed into the request.
     * @param id the identifier of the user to delete
     * @return the identifier of the deleted user
     */
    @Secured(['ROLE_ADMIN'])
    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)

        def result = null
        if (params.id == springSecurityService.principal.id) {
            result = [data: [success: false, errors: "The user can't delete herself"], status: 403]
            response.status = result.status
        } else {
            def json =JSON.parse("{id : $params.id}")
            result = processCommand(new DeleteUserCommand(user: currentUser), json)
        }
        response(result)
    }

    /*def clearUser = {
   log.info "ClearUser"
   User currentUser = getCurrentUser(springSecurityService.principal.id)
   log.info "User:" + currentUser.username + " params.idProject=" + params.id
   ///ouh le vilain code!
   Project project = Project.get(params.id)
   log.info "project= " + project
   if (project) {
     Group group = Group.findByName(project.name)
     log.info "group= " + group
     if (group) {
       ProjectGroup.unlink(project, group)
       def users = group.users();
       log.info users.size()
       users.each { user ->
         log.info "remove " + user.username + " from " + group.name
         UserGroup.unlink(user, group)
       }
       group.delete(flush:true)

       log.info "Group and project are unlink"
     }
     else {
       log.error "Cannot find group " + project.name
     }
   }
   response.status = 201
   def ret = [data: [message: "ok"], status: 201]
   response(ret)

 }   */

    @Secured(['ROLE_ADMIN'])
    def deleteUser = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)

        Project project = Project.get(params.id)
        User user = User.get(params.idUser)

        synchronized(this.getClass()) {

            if (project) {
                Group group = Group.findByName(project.name)
                log.info "group= " + group
                if (!group) {
                    group = new Group(name: project.name)
                    group.save()
                    ProjectGroup.link(project, group)
                }

                UserGroup.unlink(user, group);
            }
        }

        response.status = 201
        def ret = [data: [message: "OK"], status: 201]
        response(ret)
    }

    @Secured(['ROLE_ADMIN'])
    def addUser = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)

        Project project = Project.get(params.id)
        log.info "project= " + project

        synchronized(this.getClass()) {
            if (project) {
                Group group = Group.findByName(project.name)
                log.info "group= " + group
                if (!group) {group = new Group(name: project.name)
                    group.save()
                    ProjectGroup.link(project, group)
                }
                User user = User.get(params.idUser)
                UserGroup.link(user, group);
            }



        }
        response.status = 201
        def ret = [data: [message: "OK"], status: 201]
        response(ret)

    }

    @Secured(['ROLE_ADMIN'])
    def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder  = params.sord ?: 'asc'
        def maxRows = 50//params.row ? Integer.valueOf(params.rows) : 20
        def currentPage = params.page ? Integer.valueOf(params.page) : 1
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
        def users = User.createCriteria().list(max: maxRows, offset: rowOffset) {
            if (params.firstName)
                ilike('firstname', "%${params.firstName}%")

            if (params.lastName)
                ilike('lastname', "%${params.lastName}%")

            if (params.email)
                ilike('email', "%${params.email}%")

            order(sortIndex, sortOrder).ignoreCase()
        }

        def totalRows = users.totalCount
        def numberOfPages = Math.ceil(totalRows / maxRows)
        def jsonData = [rows: users, page: currentPage, records: totalRows, total: numberOfPages]
        render jsonData as JSON
    }

}
