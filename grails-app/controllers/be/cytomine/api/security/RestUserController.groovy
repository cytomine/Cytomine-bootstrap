package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.Utils
import grails.converters.JSON

/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
class RestUserController extends RestController {

    def springSecurityService
    def transactionService
    def cytomineService
    def secUserService
    def projectService
    def ontologyService
    def imageInstanceService

    /**
     * Get all project users
     * Online flag may be set to get only online users
     */
    def showByProject = {
        boolean online = params.boolean('online')
        Project project = projectService.read(params.long('id'))
        if (project && !online) {
            responseSuccess(secUserService.listUsers(project))
        } else if (project && online) {
            def users = secUserService.getAllFriendsUsersOnline(cytomineService.currentUser, project)
            responseSuccess(users)
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get all project admin
     */
    def showAdminByProject = {
        Project project = projectService.read(params.long('id'))
        if (project) {
            responseSuccess(secUserService.listAdmins(project))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get project creator
     */
    def showCreatorByProject = {
        Project project = projectService.read(params.long('id'))
        if (project) {
            responseSuccess([secUserService.listCreator(project)])
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get ontology creator
     */
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
    def showUserByOntology = {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess(secUserService.listUsers(ontology))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Get all user layers available for a project
     */
    def showLayerByProject = {
        Project project = projectService.read(params.long('id'))
        if (project) {
            responseSuccess(secUserService.listLayers(project))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    def list = {
        if (params.publicKey != null) {
            responseSuccess(secUserService.getByPublicKey(params.publicKey))
        } else {
            responseSuccess(secUserService.list())
        }
    }

    /**
     * Render and return an User into the specified format given in the request
     * @param id the user identifier
     * @return user an User into the specified format
     */
    def show = {
        SecUser user = secUserService.read(params.long('id'))
        if (user) {
            responseSuccess(user)
        } else {
            responseNotFound("User", params.id)
        }
    }

    /**
     * Get current user info
     */
    def showCurrent = {
        responseSuccess(secUserService.readCurrentUser())
    }


    /**
     * Add a new user
     */
    def add = {
        add(secUserService, request.JSON)
    }

    /**
     * Update a user
     */
    def update = {
        update(secUserService, request.JSON)
    }

    /**
     * Delete a user
     */
    def delete = {
        delete(secUserService, JSON.parse("{id : $params.id}"))
    }

    /**
     * Add a user to project user list
     */
    def addUserToProject = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        log.info "addUserToProject project=${project} user=${user}"
        secUserService.addUserFromProject(user, project, false)
        log.info "addUserToProject ok"
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
        secUserService.deleteUserFromProject(user, project, false)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    /**
     * Add user in project admin list
     */
    def addUserAdminToProject = {
        Project project = Project.get(params.id)
        User user = User.get(params.idUser)
        secUserService.addUserFromProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)

    }

    /**
     * Delete user from project admin list
     */
    def deleteUserAdminFromProject = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        secUserService.deleteUserFromProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    /**
     * Get all user friend (other user that share same project)
     */
    def listFriends = {
        SecUser user = secUserService.get(params.long('id'))
        Project project = null
        if (params.long('project')) {
            project = projectService.read(params.long('project'))
        }
        boolean includeOffline = params.boolean('offline')

        List<SecUser> users
        if (!includeOffline) {
            if (project) {
                //get user project online
                users = secUserService.getAllFriendsUsersOnline(user, project)
            } else {
                //get friends online
                users = secUserService.getAllFriendsUsersOnline(user)
            }
        } else {
            if (project) {
                //get all user project list
                users = secUserService.listUsers(project)
            } else {
                //get all people that share common project with user
                users = secUserService.getAllFriendsUsers(user)
            }
        }
        responseSuccess(users)
    }

    /**
     * List people connected now to the same project and get their openned pictures
     */
    def listOnlineFriendsWithPosition = {
        Project project = projectService.read(params.long('id'))
        //= now - some seconds
        Date someSecondesBefore = Utils.getDatePlusSecond(-20)

        //Get all project user online
        def users = secUserService.getAllFriendsUsersOnline(cytomineService.currentUser, project)
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
