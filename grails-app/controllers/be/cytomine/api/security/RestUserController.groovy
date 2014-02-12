package be.cytomine.api.security

import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.SecurityUtils
import be.cytomine.utils.Utils
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType


/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
@Api(name = "user services", description = "Methods for managing a user role")
class RestUserController extends RestController {

    def springSecurityService
    def cytomineService
    def secUserService
    def projectService
    def ontologyService
    def imageInstanceService

    /**
     * Get all project users
     * Online flag may be set to get only online users
     */
    @ApiMethodLight(description="Get all project users. Online flag may be set to get only online users", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="online", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional, default false) Get only online users for this project"),
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
    @ApiMethodLight(description="Get all project admin", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
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
    @ApiMethodLight(description="Get project creator (Only 1 even if response is list)", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
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
    @ApiMethodLight(description="Get ontology creator (Only 1 even if response is list)", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The ontology id")
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
    @ApiMethodLight(description="Get all ontology users. Online flag may be set to get only online users", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The ontology id")
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
    @ApiMethodLight(
            description="Get all user layers available for a project. If image param is set, add user job layers. The result depends on the current user and the project flag (hideUsersLayers,...).",
            listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="image", type="long", paramType = ApiParamType.PATH, description = "(Optional) The image id, if set add userjob layers"),
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

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    @ApiMethodLight(description="Render and returns all Users",listing = true)
    @ApiParams(params=[
        @ApiParam(name="publicKey", type="string", paramType = ApiParamType.QUERY, description = "(Optional) If set, get only user with the public key in param"),
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
    @ApiMethodLight(description="Get a user")
    @ApiParams(params=[
        @ApiParam(name="id", type="long/string", paramType = ApiParamType.PATH, description = "The user id or the user username")
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

    @ApiMethodLight(description="Get the public and private key for a user. Request only available for Admin or if user is the current user")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "(Optional) The user id"),
        @ApiParam(name="publicKey", type="string", paramType = ApiParamType.PATH, description = "(Optional) The user key")
    ])
    @ApiResponseObject(objectIdentifier = "[publicKey:x, privateKey:x]")
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
        SecurityACL.checkIsSameUser(user,cytomineService.currentUser)
        if (user) {
            responseSuccess([publicKey:user.publicKey,privateKey:user.privateKey])
        } else {
            responseNotFound("User", params.id)
        }
    }

    @ApiMethodLight(description="Build a signature string based on params for the current user.")
    @ApiParams(params=[
        @ApiParam(name="method", type="string", paramType = ApiParamType.QUERY, description = "The request method action"),
        @ApiParam(name="content-MD5", type="string", paramType = ApiParamType.QUERY, description = "(Optional) The request MD5"),
        @ApiParam(name="content-type", type="string", paramType = ApiParamType.QUERY, description = "(Optional) The request content type"),
        @ApiParam(name="date", type="string", paramType = ApiParamType.QUERY, description = "(Optional) The request date"),
        @ApiParam(name="queryString", type="string", paramType = ApiParamType.QUERY, description = "(Optional) The request query string"),
        @ApiParam(name="forwardURI", type="string", paramType = ApiParamType.QUERY, description = "(Optional) The request forward URI")
        ])
    @ApiResponseObject(objectIdentifier = "[signature:x, publicKey:x]")
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
    @ApiMethodLight(description="Get current user info")
    def showCurrent() {
        responseSuccess(secUserService.readCurrentUser())
    }


    /**
     * Add a new user
     */
    @ApiMethodLight(description="Add a user, by default the sec role 'USER' is set")
    def add() {
        add(secUserService, request.JSON)
    }

    /**
     * Update a user
     */
    @ApiMethodLight(description="Edit a user")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    def update() {
        update(secUserService, request.JSON)
    }

    /**
     * Delete a user
     */
    @ApiMethodLight(description="Delete a user")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    def delete() {
        delete(secUserService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Add a user to project user list
     */
    @ApiMethodLight(description="Add user in a project as simple 'user'")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="idUser", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
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
    @ApiMethodLight(description="Delete user from a project as simple 'user'")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="idUser", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
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
    @ApiMethodLight(description="Add user in project admin list")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="idUser", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
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
    @ApiMethodLight(description="Delete user from project admin list")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="idUser", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
    def deleteUserAdminFromProject() {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        secUserService.deleteUserFromProject(user, project, true)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    @ApiMethodLight(description="Change a user password for a user")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParam(name="password", type="string", paramType = ApiParamType.QUERY, description = "The new password")
    ])
    def resetPassword () {
        try {
        SecUser user = SecUser.get(params.long('id'))
        String oldPassword = params.get('oldPassword')
        oldPassword = springSecurityService.encodePassword(oldPassword)
        if (user.password != oldPassword && !user.passwordExpired) {
            responseNotFound("Password",params.password)
        }
        String newPassword = params.get('password')
        log.info "change password for user $user with new password $newPassword"
        if(user && newPassword) {
            SecurityACL.checkIsCreator(user,cytomineService.currentUser)
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
    @ApiMethodLight(description="Get all user friend (other user that share same project) for a specific user", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The user id"),
        @ApiParam(name="project", type="long", paramType = ApiParamType.QUERY, description = "The project id"),
        @ApiParam(name="offline", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional, default false) Get online and offline user")
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
    @ApiMethodLight(description="List people connected now to the same project and get their openned pictures", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
    ])
    @ApiResponseObject(objectIdentifier = "List of [id: %idUser%,image: %idImage%, filename: %Image path%, originalFilename:%Image filename%, date: %Last position date%]")
    def listOnlineFriendsWithPosition() {
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
        def userInfo = [:]
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
            userInfo['position'] << [id: it[1],image: it[1], filename: it[2], originalFilename:it[2], date: it[3]]
            previousUser = currenUser
        }
        //user online with no image open
        usersId.each {
            usersWithPosition << [id: it, position: []]
        }
        responseSuccess(usersWithPosition)
    }

    def CASLdapUserDetailsService

    @ApiMethodLight(description="Add a user from the LDAP", listing = true)
    @ApiParams(params=[
        @ApiParam(name="username", type="long", paramType = ApiParamType.QUERY, description = "The username in LDAP"),
    ])
    def addFromLDAP() {
        log.info  "username = " + params.username  + " role = " + params.role
        CASLdapUserDetailsService.loadUserByUsername(params.username)
        def resp = SecUser.findByUsername(params.username)
        log.info resp
        responseSuccess(resp)
    }
}
