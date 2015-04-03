package be.cytomine.api.project

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.Task
import grails.converters.JSON
import groovy.sql.Sql
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for project domain
 * A project has some images and a set of annotation
 * Users can access to project with Spring security Acl plugin
 */
@RestApi(name = "project services", description = "Methods for managing projects")
class RestProjectController extends RestController {

    def springSecurityService
    def projectService
    def ontologyService
    def cytomineService
    def imageInstanceService
    def taskService
    def secUserService
    def dataSource
    def currentRoleServiceProxy

    /**
     * List all project available for the current user
     */
    @RestApiMethod(description="Get project listing, according to your access", listing=true)
    def list() {
        SecUser user = cytomineService.currentUser
        if(currentRoleServiceProxy.isAdminByNow(user)) {
            //if user is admin, we print all available project
            responseSuccess(projectService.list())
        } else {
            // better perf with this direct hql request on spring security acl domain table (than post filter)
            //responseSuccess(projectService.list(user))
            responseSuccess(projectService.list(user))
        }
    }

    /**
     * Get a project
     */
    @RestApiMethod(description="Get a project")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def show () {
        println request.session.id
        Project project = projectService.read(params.long('id'))
        if (project) {
            responseSuccess(project)
        } else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * Add a new project to cytomine
     */
    @RestApiMethod(description="Add a new project")
    def add() {
        log.info "Add project = $request.JSON"
        try {
            Task task = taskService.read(params.getLong("task"))
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def result = projectService.add(request.JSON,task)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Update a project
     */
    @RestApiMethod(description="Update a project")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="int", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def update () {
        try {
            Task task = taskService.read(params.getLong("task"))
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def domain = projectService.retrieve(request.JSON)
            def result = projectService.update(domain,request.JSON,task)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    /**
     * Delete a project
     */
    @RestApiMethod(description="Delete a project")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id")
    ])
    def delete () {
        try {
            Task task = taskService.read(params.getLong("task"))
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def domain = projectService.retrieve(JSON.parse("{id : $params.id}"))
            log.info "project = ${domain}"
            def result = projectService.delete(domain,transactionService.start(),task)
            //delete container in retrieval
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Get last action done on a specific project
     * ex: "user x add a new annotation on image y",...
     */
    @RestApiMethod(description="Get the last action for a project", listing = true)
    @RestApiResponseObject(objectIdentifier="command history")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id")
    ])
    def lastAction() {
        Project project = projectService.read(params.long('id'))
        int max = Integer.parseInt(params.max);

        if (project) {
            responseSuccess(projectService.lastAction(project, max))
        } else {
            responseNotFound("Project", params.id)
        }
    }

    @RestApiMethod(description="Get the last opened projects for the current user", listing = true)
    def listLastOpened() {
        SecUser user = cytomineService.currentUser
        responseSuccess(projectService.listLastOpened(user, params.long('max')))
    }

    /**
     * List all project available for this user, that can use a software
     */
    @RestApiMethod(description="Get projects available for the current user that can use a specific software", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The software id")
    ])
    def listBySoftware() {
        Software software = Software.read(params.long('id'))
        if(software) {
            responseSuccess(projectService.list(software))
        } else {
            responseNotFound("Software", params.id)
        }
    }

    /**
     * List all project available for this user, that use a ontology
     */
    @RestApiMethod(description="Get projects available for the current user that can use a specific ontology", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The ontology id")
    ])
    def listByOntology() {
        Ontology ontology = ontologyService.read(params.long('id'));
        if (ontology != null) {
            responseSuccess(projectService.list(ontology))
        } else {
            responseNotFound("Project", "Ontology", params.id)
        }
    }

    /**
     * List all project available for the current user, that can be used by a user
     */
    @RestApiMethod(description="Get projects available for the current user and available for a specific user", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The user id")
    ])
    def listByUser() {
        User user = User.read(params.long('id'))
        if(user) {
            responseSuccess(projectService.list(user))
        } else {
            responseNotFound("User", params.id)
        }
    }

    /**
     * List all project available for the current user
     */
    @RestApiMethod(description="Get projects available for the current user and available for a specific user in a specific role (user, admin, creator). ", listing = true)
    @RestApiResponseObject(objectIdentifier="project (light)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The user id"),
        @RestApiParam(name="creator", type="boolean", paramType = RestApiParamType.QUERY,description = "filter by creator"),
        @RestApiParam(name="admin", type="boolean", paramType = RestApiParamType.QUERY,description = "filter by admin"),
        @RestApiParam(name="user", type="boolean", paramType = RestApiParamType.QUERY,description = "filter by user")
    ])
    def listLightByUser() {
        User user = secUserService.read(params.long('id'))
        boolean creator = params.getBoolean('creator')
        boolean admins = params.getBoolean('admin')
        boolean users = params.getBoolean('user')
        if(!user) {
            responseNotFound("User", params.id)
        } else if(creator) {
            responseSuccess(projectService.listByCreator(user))
        } else if(admins) {
            responseSuccess(projectService.listByAdmin(user))
        } else if(users) {
            responseSuccess(projectService.listByUser(user))
        }  else {
            responseSuccess(projectService.listByUser(user))
        }
    }

    /**
     * List all retrieval-project for a specific project
     * The suggested term can use data from other project (with same ontology).
     */
    @RestApiMethod(description="List all retrieval-project for a specific project. The suggested term can use data from other project (with same ontology).", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
    ])
    def listRetrieval() {
        Project project = projectService.read(params.long('id'))
        if (project) {
            def projects = project.retrievalProjects.findAll{!it.deleted}
            responseSuccess(projects)
        } else {
            responseNotFound("Project", params.id)
        }
    }

    @RestApiMethod(description="Get the last action for a user in a project or in all projects available for the current user", listing = true)
    @RestApiResponseObject(objectIdentifier="commandHistory")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id (if null: all projects)"),
            @RestApiParam(name="user", type="long", paramType = RestApiParamType.QUERY,description = "The user id"),
            @RestApiParam(name="fullData", type="boolean", paramType = RestApiParamType.QUERY,description = "Flag to include the full JSON of the data field on each command history. Not recommended for long listing.")
    ])
    def listCommandHistory() {
        Project project = projectService.read(params.long('id'))
        Integer offset = params.offset != null ? params.getInt('offset') : 0
        Integer max = (params.max != null && params.getInt('max')!=0) ? params.getInt('max') : Integer.MAX_VALUE
        SecUser user = secUserService.read(params.long('user'))
        Boolean fullData = params.getBoolean('fullData')
        if (project) {
            response(findCommandHistory([project],user,max,offset,fullData))
        } else {
            //no project defined, get all user projects
            List<Project> projects = projectService.list(cytomineService.currentUser);
            response(findCommandHistory(projects,user,max,offset,fullData))
        }
    }

    @RestApiMethod(description="Invite a not yer existing user to the project")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
            @RestApiParam(name="json", type="string", paramType = RestApiParamType.QUERY,description = "The user name and email of the invited user"),
    ])
    def inviteNewUser() {
        Project project = projectService.read(params.long('id'))

        try {
            def result = projectService.inviteUser(project, request.JSON);
            responseSuccess(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    private def findCommandHistory(List<Project> projects,SecUser user, Integer max, Integer offset, Boolean fullData) {

        String request;

        if(fullData) {
            request = "SELECT ch.id as id, ch.created as created, ch.message as message, ch.prefix_action as prefixAction, ch.user_id as user, ch.project_id as project, c.data as data,c.service_name as serviceName, c.class as className, c.action_message as actionMessage, u.username as username " +
                    "FROM command_history ch, command c, sec_user u " +
                    "WHERE ch.command_id = c.id AND u.id = ch.user_id " +
                    (projects? "AND ch.project_id IN (${projects.collect{it.id}.join(",")}) " : " ") +
                    (user? "AND ch.user_id =  ${user.id} " : " ") +
                    "ORDER BY created desc LIMIT $max OFFSET $offset"
        } else {
            request = "SELECT ch.id as id, ch.created as created, ch.message as message, ch.prefix_action as prefixAction, ch.user_id as user, ch.project_id as project " +
                    "FROM command_history ch " +
                    "WHERE true  " +
                    (projects? "AND ch.project_id IN (${projects.collect{it.id}.join(",")}) " : " ") +
                    (user? "AND ch.user_id =  ${user.id} " : " ") +
                    "ORDER BY created desc LIMIT $max OFFSET $offset"
        }
        def result = doGenericRequest(request,fullData)
        return result
    }

    private def doGenericRequest(String request,Boolean fullData) {
        def data = []
        Long start = System.currentTimeMillis()

        def sql = new Sql(dataSource)
         sql.eachRow(request) {
            if(data.isEmpty()) {
                start = System.currentTimeMillis()
            }
            def line = [id:it.id,created:it.created,message:it.message,prefix:it.prefixAction,prefixAction:it.prefixAction,user:it.user,project:it.project]
            if(fullData) {
                line.data = it.data
                line.serviceName = it.serviceName
                line.className = it.className
                line.action = it.actionMessage + " by " + it.username
            }
            data << line

        }
        sql.close()
        data
    }

}

