package be.cytomine.api.security

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.SecurityUtils
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import org.joda.time.DateTime
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
@RestApi(name = "user services", description = "Methods for managing a user role")
class RestUserController extends RestController {

    def springSecurityService
    def cytomineService
    def secUserService
    def projectService
    def ontologyService
    def imageInstanceService
    def groupService
    def securityACLService
    def mongo
    def noSQLCollectionService
    def reportService

    /**
     * Get all project users
     * Online flag may be set to get only online users
     */
    @RestApiMethod(description="Get all project users. Online flag may be set to get only online users", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
        @RestApiParam(name="online", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional, default false) Get only online users for this project"),
    ])
    def showByProject() {
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
    @RestApiMethod(description="Get all project admin", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def showAdminByProject() {
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
    @RestApiMethod(description="Get project creator (Only 1 even if response is list)", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def showCreatorByProject() {
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
    @RestApiMethod(description="Get ontology creator (Only 1 even if response is list)", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The ontology id")
    ])
    def showCreatorByOntology() {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess([ontology.user])
        }
        else responseNotFound("User", "Project", params.id)
    }

    /**
     * Get ontology user list
     */
    @RestApiMethod(description="Get all ontology users. Online flag may be set to get only online users", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The ontology id")
    ])
    def showUserByOntology() {
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
    @RestApiMethod(
            description="Get all user layers available for a project. If image param is set, add user job layers. The result depends on the current user and the project flag (hideUsersLayers,...).",
            listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
        @RestApiParam(name="image", type="long", paramType = RestApiParamType.PATH, description = "(Optional) The image id, if set add userjob layers"),
    ])
    def showLayerByProject() {
        Project project = projectService.read(params.long('id'))
        ImageInstance image = imageInstanceService.read(params.long('image'))
        if (project) {
            responseSuccess(secUserService.listLayers(project,image))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    def listByGroup() {
        Group group = groupService.read(params.long('id'))
        if (group) {
            responseSuccess(secUserService.listByGroup(group))
        } else {
            responseNotFound("User", "Project", params.id)
        }
    }

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    @RestApiMethod(description="Render and returns all Users",listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="publicKey", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) If set, get only user with the public key in param"),
    ])
    def list() {
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
    @RestApiMethod(description="Get a user")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long/string", paramType = RestApiParamType.PATH, description = "The user id or the user username")
    ])
    def show() {
        def id = params.long('id')
        SecUser user
        if(id) {
            user = secUserService.read(id)
        } else {
            user = secUserService.findByUsername(params.id)
        }

        if (user) {
            def  maps = JSON.parse(user.encodeAsJSON())
            def  authMaps = secUserService.getAuth(user)
            maps.admin = authMaps.get("admin")
            maps.user = authMaps.get("user")
            maps.guest = authMaps.get("guest")
           responseSuccess(maps)
//            responseSuccess(user)
        } else {
            responseNotFound("User", params.id)
        }
    }

    @RestApiMethod(description="Get the public and private key for a user. Request only available for Admin or if user is the current user")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "(Optional) The user id"),
        @RestApiParam(name="publicKey", type="string", paramType = RestApiParamType.PATH, description = "(Optional) The user key")
    ])
    @RestApiResponseObject(objectIdentifier = "[publicKey:x, privateKey:x]")
    def keys() {
        def publicKey = params.publicKey
        def id = params.long('id')
        SecUser user

        if(publicKey) {
            user = SecUser.findByPublicKey(publicKey)
        } else if(id) {
            user = secUserService.read(id)
        } else {
            user = secUserService.findByUsername(params.id)
        }
        securityACLService.checkIsSameUser(user,cytomineService.currentUser)
        if (user) {
            responseSuccess([publicKey:user.publicKey,privateKey:user.privateKey])
        } else {
            responseNotFound("User", params.id)
        }
    }

    @RestApiMethod(description="Build a signature string based on params for the current user.")
    @RestApiParams(params=[
        @RestApiParam(name="method", type="string", paramType = RestApiParamType.QUERY, description = "The request method action"),
        @RestApiParam(name="content-MD5", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) The request MD5"),
        @RestApiParam(name="content-type", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) The request content type"),
        @RestApiParam(name="date", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) The request date"),
        @RestApiParam(name="queryString", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) The request query string"),
        @RestApiParam(name="forwardURI", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) The request forward URI")
        ])
    @RestApiResponseObject(objectIdentifier = "[signature:x, publicKey:x]")
    def signature() {
        SecUser user = cytomineService.currentUser

        String method = params.get('method')
        String content_md5 = (params.get("content-MD5") != null) ? params.get("content-MD5") : ""
        String content_type = (params.get("content-type") != null) ? params.get("content-type") : ""
        content_type = (params.get("Content-Type") != null) ? params.get("Content-Type") : content_type
        String date = (params.get("date") != null) ? params.get("date") : ""
        String queryString = (params.get("queryString") != null) ? "?" + params.get("queryString") : ""
        String path = params.get('forwardURI') //original URI Request

        log.info "user=$user"
        log.info "content_md5=$content_md5"
        log.info "content_type=$content_type"
        log.info "date=$date"
        log.info "queryString=$queryString"
        log.info "path=$path"
        log.info "method=$method"

        String signature = SecurityUtils.generateKeys(method,content_md5,content_type,date,queryString,path,user)

        responseSuccess([signature:signature, publicKey:user.getPublicKey()])
    }

    /**
     * Get current user info
     */
    @RestApiMethod(description="Get current user info")
    def showCurrent() {
        SecUser user = secUserService.readCurrentUser()
        def  maps = JSON.parse(user.encodeAsJSON())
        def  authMaps = secUserService.getAuth(user)
        maps.admin = authMaps.get("admin")
        maps.user = authMaps.get("user")
        maps.guest = authMaps.get("guest")
        maps.adminByNow = authMaps.get("adminByNow")
        maps.userByNow = authMaps.get("userByNow")
        maps.guestByNow = authMaps.get("guestByNow")
        maps.isSwitched = SpringSecurityUtils.isSwitched()
        if(maps.isSwitched) {
            maps.realUser = SpringSecurityUtils.switchedUserOriginalUsername
        }
        responseSuccess(maps)
    }



    /**
     * Add a new user
     */
    @RestApiMethod(description="Add a user, by default the sec role 'USER' is set")
    def add() {
        add(secUserService, request.JSON)
    }

    /**
     * Update a user
     */
    @RestApiMethod(description="Edit a user")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    def update() {
        update(secUserService, request.JSON)
    }

    /**
     * Delete a user
     */
    @RestApiMethod(description="Delete a user")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    def delete() {
        delete(secUserService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Add a user to project user list
     */
    @RestApiMethod(description="Add user in a project as simple 'user'")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
        @RestApiParam(name="idUser", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def addUserToProject() {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        log.info "addUserToProject project=${project} user=${user}"
        secUserService.addUserToProject(user, project, false)
        log.info "addUserToProject ok"
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)

    }

    /**
     * Delete a user from a project user list
     */
    @RestApiMethod(description="Delete user from a project as simple 'user'")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
        @RestApiParam(name="idUser", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def deleteUserFromProject() {
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
    @RestApiMethod(description="Add user in project admin list")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
        @RestApiParam(name="idUser", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def addUserAdminToProject() {
        Project project = Project.get(params.id)
        User user = User.get(params.idUser)
        secUserService.addUserToProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)

    }

    /**
     * Delete user from project admin list
     */
    @RestApiMethod(description="Delete user from project admin list")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
        @RestApiParam(name="idUser", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def deleteUserAdminFromProject() {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        secUserService.deleteUserFromProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    @RestApiMethod(description="Change a user password for a user")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="password", type="string", paramType = RestApiParamType.QUERY, description = "The new password")
    ])
    def resetPassword () {
        try {
        SecUser user = SecUser.get(params.long('id'))
        //::todo : check also old password security
        /*String oldPassword = params.get('oldPassword')
        oldPassword = springSecurityService.encodePassword(oldPassword)
        if (user.password != oldPassword && !user.passwordExpired) {
            responseNotFound("Password",params.password)
        } */
        String newPassword = params.get('password')
        log.info "change password for user $user with new password $newPassword"
        if(user && newPassword) {
            securityACLService.checkIsCreator(user,cytomineService.currentUser)
            user.newPassword = newPassword
            //force to reset password (newPassword is transient => beforeupdate is not called):
            user.password = "bad"
            secUserService.saveDomain(user)
            response(user)
        } else if(!user) {
            responseNotFound("SecUser",params.id)
        }else if(!newPassword) {
            responseNotFound("Password",params.password)
         }
        }catch(CytomineException e) {
            responseError(e)
        }

    }

    /**
     * Get all user friend (other user that share same project)
     */
    @RestApiMethod(description="Get all user friend (other user that share same project) for a specific user", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.QUERY, description = "The project id"),
        @RestApiParam(name="offline", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional, default false) Get online and offline user")
    ])
    def listFriends() {
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
    @RestApiMethod(description="List people connected now to the same project and get their openned pictures", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    @RestApiResponseObject(objectIdentifier = "List of [id: %idUser%,image: %idImage%, filename: %Image path%, originalFilename:%Image filename%, date: %Last position date%]")
    def listOnlineFriendsWithPosition() {
//        Project project = projectService.read(params.long('id'))
//
//        //Get all project user online
//        def users = secUserService.getAllFriendsUsersOnline(cytomineService.currentUser, project)
//        def usersId = users.collect {it.id}
//
//        Get all user oonline and their pictures
//        List<SecUser> userPositions = SecUser.executeQuery(
//                "SELECT userPosition.user.id,imageInstance.id, abstractImage.originalFilename, max(userPosition.updated) from UserPosition as userPosition, ImageInstance as imageInstance, AbstractImage as abstractImage " +
//                        "where userPosition.project.id = ${project.id} and userPosition.updated > ? and imageInstance.id = userPosition.image.id and imageInstance.baseImage.id = abstractImage.id group by userPosition.user.id,imageInstance.id,abstractImage.originalFilename order by userPosition.user.id", [someSecondesBefore])
//
//
//        def usersWithPosition = []
//        def userInfo = [:]
//        long previousUser = -1
//        userPositions.each {
//            long currenUser = it[0]
//            if (previousUser != currenUser) {
//                //new user, create a new line
//                userInfo = [id: currenUser, position: []]
//                usersWithPosition << userInfo
//                usersId.remove(currenUser)
//            }
//            //add position to the current user
//            userInfo['position'] << [id: it[1],image: it[1], filename: it[2], originalFilename:it[2], date: it[3]]
//            previousUser = currenUser
//        }
//        //user online with no image open
//        usersId.each {
//            usersWithPosition << [id: it, position: []]
//        }
//        responseSuccess(usersWithPosition)
//        responseSuccess([])

        Project project = projectService.read(params.long('id'))

        //Get all project user online
        def users = secUserService.getAllFriendsUsersOnline(cytomineService.currentUser, project)
        def usersId = users.collect {it}

        //Get all user oonline and their pictures
        def db = mongo.getDB(noSQLCollectionService.getDatabaseName())
        DateTime thirtySecondsAgo = new DateTime().minusSeconds(30)
        def result = db.lastUserPosition.aggregate(
                [$match : [ project : project.id, created:[$gt:thirtySecondsAgo.toDate()]]],
                [$project:[user:1,image:1,imageName:1,created:1]],
                [$group : [_id : [ user: '$user', image: '$image',imageName: '$imageName'], "date":[$max:'$created']]]
        )

        def usersWithPosition = []
        def userInfo = [:]
        long previousUser = -1
        result.results().each {

            def userId = it["_id"]["user"]
            def imageId = it["_id"]["image"]
            def imageName = it["_id"]["imageName"]
            def date = it["date"]

            long currenUser = userId
            if (previousUser != currenUser) {
                //new user, create a new line
                userInfo = [id: currenUser, position: []]
                usersWithPosition << userInfo
                usersId.remove(currenUser)
            }
            //add position to the current user
            userInfo['position'] << [id: imageId,image: imageId, filename: imageName, originalFilename:imageName, date: date]
            previousUser = currenUser
        }
        //user online with no image open
        usersId.each {
            usersWithPosition << [id: it, position: []]
        }
        responseSuccess(usersWithPosition)
//        responseSuccess([])
    }

    def CASLdapUserDetailsService

    @RestApiMethod(description="Add an user from the LDAP", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="username", type="long", paramType = RestApiParamType.QUERY, description = "The username in LDAP"),
    ])
    def addFromLDAP() {
        log.info  "username = " + params.username  + " role = " + params.role
        CASLdapUserDetailsService.loadUserByUsername(params.username)
        def resp = SecUser.findByUsername(params.username)
        log.info resp
        responseSuccess(resp)
    }

    @RestApiMethod(description="Check if an user is in the LDAP", listing = true)
    @RestApiParams(params=[
            @RestApiParam(name="username", type="long", paramType = RestApiParamType.QUERY, description = "The username in LDAP"),
    ])
    def isInLdap() {
        def result = CASLdapUserDetailsService.isInLdap(params.username)
        def returnArray = [:]
        returnArray["result"] = result
        responseSuccess(returnArray)
    }

    @RestApiMethod(description="Download a report (pdf, xls,...) with user listing from a specific project")
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
            @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
            @RestApiParam(name="users", type="list", paramType = RestApiParamType.QUERY,description = "The annotation users id (if empty: all users)"),
            @RestApiParam(name="images", type="list", paramType = RestApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
            @RestApiParam(name="format", type="string", paramType = RestApiParamType.QUERY,description = "The report format (pdf, xls,...)")
    ])
    def downloadUserListingLightByProject() {
        reportService.createUserListingLightDocuments(params.long('id'),params.format,response)
    }

}
