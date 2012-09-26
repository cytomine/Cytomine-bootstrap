package be.cytomine.api.security

import grails.plugins.springsecurity.Secured

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import grails.converters.JSON

import java.text.SimpleDateFormat
import be.cytomine.ontology.Ontology
import be.cytomine.social.LastConnection
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.apache.commons.collections.ListUtils
import be.cytomine.social.UserPosition
import be.cytomine.utils.Utils

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

    /**
     * Render and returns all Users into the specified format given in the request
     * @return all Users into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        if(params.publicKey!=null) {
            responseSuccess(userService.getByPublicKey(params.publicKey))
        } else responseSuccess(userService.list())
    }

    def showUserJob = {
        UserJob userJob = UserJob.read(params.long('id'))
        if(userJob) responseSuccess(userJob)
        else responseNotFound("UserJob", params.id)

    }

    /**
     * Render and return an User into the specified format given in the request
     * @param id the user identifier
     * @return user an User into the specified format
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        SecUser user = userService.read(params.long('id'))
        if (user) responseSuccess(user)
        else responseNotFound("User", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCurrent = {
        responseSuccess(userService.readCurrentUser())
    }

//    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
//    def showByProject = {
//        Project project = projectService.read(params.long('id'))
//        if (project) responseSuccess(project.users())
//        else responseNotFound("User", "Project", params.id)
//    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showByProject = {
        Project project = projectService.read(params.long('id'),new Project())
        if (project) {
            responseSuccess(project.users())
        }
        else responseNotFound("User", "Project", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showAdminByProject = {
        Project project = projectService.read(params.long('id'),new Project())
        if (project) {
            responseSuccess(project.admins())
        }
        else responseNotFound("User", "Project", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCreatorByProject = {
        Project project = projectService.read(params.long('id'),new Project())
        if (project) {
            responseSuccess([project.creator()])
        }
        else responseNotFound("User", "Project", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showCreatorByOntology = {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess([ontology.user])
        }
        else responseNotFound("User", "Project", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showUserByOntology = {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) {
            responseSuccess(ontology.users())
        }
        else responseNotFound("User", "Project", params.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def showLayerByProject = {
        Project project = projectService.read(params.long('id'),new Project())
        if (project) {
            responseSuccess(project.userLayers())
        }
        else responseNotFound("User", "Project", params.id)
    }




    @Secured(['ROLE_ADMIN'])
    def add = {
        add(userService, request.JSON)
    }
    @Secured(['ROLE_USER','ROLE_ADMIN'])
    def update = {
        update(userService, request.JSON)
    }
    @Secured(['ROLE_ADMIN'])
    def delete = {
        delete(userService, JSON.parse("{id : $params.id}"))
    }

    def addChild = {
        def json = request.JSON
        User user = null
        if(json.parent.toString().equals("null")) {
            user = User.read(springSecurityService.principal.id)
        } else {
            user = User.read(json.parent.toString())
        }

        UserJob userJob = new UserJob()
        if(json.job.toString().equals("null")) {
            log.debug "Job is not define: create new job:"+json
            Job job = new Job()
            job.software = Software.read(json.software)
            try {
                job.project = Project.read(json.project)
            }catch(Exception e) {
                log.warn e.toString()
            }
            if (job.validate()) {
                job = job.save(flush : true)
            } else {
                job.errors?.each { log.warn it}
            }
            userJob.job = job
        } else {

            log.debug "Job is define: add job " + json.job + " to userjob"
            Job job = Job.get(Long.parseLong(json.job.toString()))
            userJob.job = job
        }
        log.debug "Create userJob"
        userJob.username = "JOB[" + user.username + "], " + new Date().toString()
        userJob.password = user.password
        userJob.generateKeys()
        userJob.enabled = user.enabled
        userJob.accountExpired = user.accountExpired
        userJob.accountLocked = user.accountLocked
        userJob.passwordExpired = user.passwordExpired
        userJob.user = user
        try {
            Date date = new Date()
            date.setTime(Long.parseLong(json.created.toString()))
            userJob.created = date
        } catch(Exception e) {log.warn e.toString()}
        userJob = userJob.save(flush:true)

        user.getAuthorities().each { secRole ->
            SecUserSecRole.create(userJob, secRole)
        }

//        projectService.list().each {
//            userService.addUserFromProject(userJob,it,true)
//        }

        //def ret = [data: [user: newUser], status: 200]
        response([userJob: userJob],200)

    }

    def deleteUser = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        boolean admin = false
        userService.deleteUserFromProject(user,project,admin)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    def addUser = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        boolean admin = false
        userService.addUserFromProject(user,project,admin)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)

    }

    @Secured(['ROLE_ADMIN'])
    def deleteUserAdmin = {
        Project project = Project.get(params.id)
        SecUser user = SecUser.get(params.idUser)
        boolean admin = true
        userService.deleteUserFromProject(user,project,admin)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
        response(ret)
    }

    @Secured(['ROLE_ADMIN'])
    def addUserAdmin = {
        Project project = Project.get(params.id)
        User user = User.get(params.idUser)
        boolean admin = true
        userService.addUserFromProject(user,project,admin)
        response.status = 200
        def ret = [data: [message: "OK"], status: 200]
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

    def listFriends = {
        SecUser user = userService.get(params.long('id'))
        Long idProject = params.long('project')
        Project project = null
        if(idProject) project = projectService.read(params.long('project'),new Project())
        boolean includeOffline = params.boolean('offline')

        List<SecUser> users
        if(!includeOffline){
            if(project)
                users = userService.getAllFriendsUsersOnline(user,project)
            else users = userService.getAllFriendsUsersOnline(user)
        } else {
            if(project)
                users = securityService.getUserList(project)
            else
                users = userService.getAllFriendsUsers(user)
        }
        responseSuccess(users)
    }

    def listOnlineFriendsWithPosition = {
        println "listOnlineFriendsWithPosition ${params}"
        Project project = projectService.read(params.long('id'),new Project())

        def users = userService.getAllFriendsUsersOnline(cytomineService.currentUser,project)
        def userWithPosition = []
        users.each{
            def json = JSON.parse(it.encodeAsJSON())
            json.position = getLastPosition(it,project)
            userWithPosition <<  json
        }
        responseSuccess(userWithPosition)
    }

    private List getLastPosition(SecUser user, Project project) {
        println "getLastPosition ${user} ${project}"
        println UserPosition.list().get(0).updated
        Date someSecondesBefore = Utils.getDatePlusSecond(-20)

        List<SecUser> userPositions = SecUser.executeQuery(
            "SELECT image.id, project.id, max(updated) from UserPosition as userPosition "+
            "where userPosition.project.id = ${project.id} and userPosition.user.id = ${user.id} and updated > ? group by image.id,project.id",[someSecondesBefore])

        println userPositions

        def positions = []

        userPositions.each { position ->
            println position
              def mapPositon = [date:position[2],image:position[0]]
            positions <<  mapPositon

        }

        positions
    }


    def listUserJobByProject = {
        Project project = projectService.read(params.long('id'),new Project())
        if (project) {
            if(params.getBoolean("tree")) {

                SimpleDateFormat formater = new SimpleDateFormat("dd MM yyyy HH:mm:ss")

                def root = [:]
                root.isFolder = true
                root.hideCheckbox = true
                root.name = project.name
                root.title = project.name
                root.key = project.id
                root.id = project.id

                def allSofts = []
                List<SoftwareProject> softwareProject = SoftwareProject.findAllByProject(project)

                softwareProject.each {
                    Software software = it.software
                    def soft = [:]
                    soft.isFolder = true
                    soft.name = software.name
                    soft.title = software.name
                    soft.key = software.id
                    soft.id = software.id
                    soft.hideCheckbox = true

                    def softJob = []
                    List<Job> jobs = Job.findAllByProjectAndSoftware(project,software,[sort:'created',order:'desc'])
                    jobs.each {
                        def userJob = UserJob.findByJob(it);
                        def job = [:]
                        if(userJob) {
                            job.id = userJob.id
                            job.key = userJob.id
                            job.title = formater.format(it.created);
                            job.date = it.created.getTime()
                            job.isFolder = false
                            //job.children = []
                            softJob << job
                        }
                    }
                    soft.children = softJob

                    allSofts << soft

                }
                root.children = allSofts
                responseSuccess(root)

            } else {
                def userJobs = []
                List<Job> allJobs = Job.findAllByProject(project,[sort:'created',order:'desc'])

                allJobs.each { job ->
                    def item = [:]
                    def userJob = UserJob.findByJob(job);
                    if(userJob) {
                        item.id = userJob.id
                        item.idJob = job.id
                        item.idSoftware = job.software.id
                        item.softwareName = job.software.name
                        item.created = job.created.getTime()
                    }
                    userJobs << item
                }
                responseSuccess(userJobs)
            }
        }else {
            responseNotFound("User", "Project", params.id)
        }
    }



}
