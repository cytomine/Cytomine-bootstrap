package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.Utils
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
class RestUserController extends RestController {

    def springSecurityService
    def transactionService
    def cytomineService
    def userService
    def securityService
    def projectService
    def ontologyService
    def imageInstanceService

    /**
     * Get all project users
     * Online flag may be set to get only online users
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showByProject = {
        boolean online = params.boolean('online')
        Project project = projectService.read(params.long('id'), new Project())
        if (project && !online) {
            responseSuccess(userService.listUsers(project))
        } else if (project && online) {
            def users = userService.getAllFriendsUsersOnline(cytomineService.currentUser, project)
            responseSuccess(users)
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get all project admin
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showAdminByProject = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) {
            responseSuccess(userService.listAdmins(project))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get project creator
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCreatorByProject = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) {
            responseSuccess([userService.listCreator(project)])
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get ontology creator
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCreatorByOntology = {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess([ontology.user])
        }
        else responseNotFound("User", "Project", params.id)
    }

    /**
     * Get ontology user list
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showUserByOntology = {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess(userService.listUsers(ontology))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get all user layers available for a project
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showLayerByProject = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) {
            responseSuccess(userService.listLayers(project))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        if (params.publicKey != null) {
            responseSuccess(userService.getByPublicKey(params.publicKey))
        } else {
            responseSuccess(userService.list())
        }
    }

    /**
     * Render and return an User into the specified format given in the request
     * @param id the user identifier
     * @return user an User into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        SecUser user = userService.read(params.long('id'))
        if (user) {
            responseSuccess(user)
        } else {
            responseNotFound("User", params.id)
        }
    }

    /**
     * Get current user info
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCurrent = {
        responseSuccess(userService.readCurrentUser())
    }


    /**
     * Add a new user
     */
    @Secured(['ROLE_ADMIN'])
    def add = {
        add(userService, request.JSON)
    }

    /**
     * Update a user
     */
    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def update = {
        update(userService, request.JSON)
    }

    /**
     * Delete a user
     */
    @Secured(['ROLE_ADMIN'])
    def delete = {
        delete(userService, JSON.parse("{id : $params.id}"))
    }

    /**
     * Add a user to project user list
     */
    def addUserToProject = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        userService.addUserFromProject(user, project, false)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)

    }

    /**
     * Delete a user from a project user list
     */
    def deleteUserFromProject = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        userService.deleteUserFromProject(user, project, false)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    /**
     * Add user in project admin list
     */
    @Secured(['ROLE_ADMIN'])
    def addUserAdminToProject = {
        Project project = Project.get(params.id)
        User user = User.get(params.idUser)
        userService.addUserFromProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)

    }

    /**
     * Delete user from project admin list
     */
    @Secured(['ROLE_ADMIN'])
    def deleteUserAdminFromProject = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        userService.deleteUserFromProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    /**
     * Print user for grid UI
     */
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

    /**
     * Get all user friend (other user that share same project)
     */
    def listFriends = {
        SecUser user = userService.get(params.long('id'))
        Project project = null
        if (params.long('project')) {
            project = projectService.read(params.long('project'), new Project())
        }
        boolean includeOffline = params.boolean('offline')

        List<SecUser> users
        if (!includeOffline) {
            if (project) {
                //get user project online
                users = userService.getAllFriendsUsersOnline(user, project)
            } else {
                //get friends online
                users = userService.getAllFriendsUsersOnline(user)
            }
        } else {
            if (project) {
                //get all user project list
                users = securityService.getUserList(project)
            } else {
                //get all people that share common project with user
                users = userService.getAllFriendsUsers(user)
            }
        }
        responseSuccess(users)
    }

    /**
     * List people connected now to the same project and get their openned pictures
     */
    def listOnlineFriendsWithPosition = {
        Project project = projectService.read(params.long('id'), new Project())
        //= now - some seconds
        Date someSecondesBefore = Utils.getDatePlusSecond(-20)

        //Get all project user online
        def users = userService.getAllFriendsUsersOnline(cytomineService.currentUser, project)
        def usersId = users.collect {it.id}

        //Get all user oonline and their pictures
        List<SecUser> userPositions = SecUser.executeQuery(
                "SELECT userPosition.user.id,imageInstance.id, abstractImage.originalFilename, max(userPosition.updated) from UserPosition as userPosition, ImageInstance as imageInstance, AbstractImage as abstractImage " +
                        "where userPosition.project.id = ${project.id} and userPosition.updated > ? and imageInstance.id = userPosition.image.id and imageInstance.baseImage.id = abstractImage.id group by userPosition.user.id,imageInstance.id,abstractImage.originalFilename order by userPosition.user.id", [someSecondesBefore])


        def usersWithPosition = []
        def userInfo
        long previousUser = -1
        userPositions.each {
            long currenUser = it[0]
            if (previousUser != currenUser) {
                //new user, create a new line
                userInfo = [id: currenUser, position: []]
                usersWithPosition << userInfo
                usersId.remove(currenUser)
            }
            //add position to the current user
            userInfo['position'] << [image: it[1], filename: it[2], date: it[3]]
            previousUser = currenUser
        }
        //user online with no image open
        usersId.each {
            usersWithPosition << [id: it, position: []]
        }
        responseSuccess(usersWithPosition)
    }
}
