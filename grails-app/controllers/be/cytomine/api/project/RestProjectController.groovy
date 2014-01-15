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
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiError
import org.jsondoc.core.annotation.ApiErrors
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for project domain
 * A project has some images and a set of annotation
 * Users can access to project with Spring security Acl plugin
 */
@Api(name = "project services", description = "Methods for managing projects")
class RestProjectController extends RestController {

    def springSecurityService
    def projectService
    def ontologyService
    def cytomineService
    def retrievalService
    def imageInstanceService
    def taskService
    def secUserService
    def dataSource

    def currentDomain() {
        Project
    }

    /**
     * List all project available for the current user
     */
    @ApiMethodLight(description="Get project listing, according to your access", listing=true)
    def list() {
        SecUser user = cytomineService.currentUser
        if(user.isAdmin()) {
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
    @ApiMethodLight(description="Get a project")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH)
    ])
    def show () {
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
    @ApiMethodLight(description="Add a new project")
    @ApiErrors(apierrors=[
        @ApiError(code="409", description="Project with same name already exist")
    ])
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
    @ApiMethodLight(description="Update a project")
    @ApiParams(params=[
        @ApiParam(name="id", type="int", paramType = ApiParamType.PATH, description = "The project id")
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
    @ApiMethodLight(description="Delete a project")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id")
    ])
    def delete () {
        try {
            Task task = taskService.read(params.getLong("task"))
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def domain = projectService.retrieve(JSON.parse("{id : $params.id}"))
            log.info "project = ${domain}"
            def result = projectService.delete(domain,transactionService.start(),task)
            //delete container in retrieval
            try {retrievalService.deleteContainerAsynchronous(params.id) } catch(Exception e) {log.error e}
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
    @ApiMethodLight(description="Get the last action for a project", listing = true)
    @ApiResponseObject(objectIdentifier="commandHistory")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id")
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

    @ApiMethodLight(description="Get the last opened projects for the current user", listing = true)
    def listLastOpened() {
        SecUser user = cytomineService.currentUser
        responseSuccess(projectService.listLastOpened(user, params.long('max')))
    }

    /**
     * List all project available for this user, that can use a software
     */
    @ApiMethodLight(description="Get projects available for the current user that can use a specific software", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The software id")
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
    @ApiMethodLight(description="Get projects available for the current user that can use a specific ontology", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The ontology id")
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
    @ApiMethodLight(description="Get projects available for the current user and available for a specific user", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The user id")
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
    @ApiMethodLight(description="Get projects available for the current user and available for a specific user in a specific role (user, admin, creator). ", listing = true)
    @ApiResponseObject(objectIdentifier="project (light)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The user id"),
        @ApiParam(name="creator", type="boolean", paramType = ApiParamType.QUERY,description = "filter by creator"),
        @ApiParam(name="admin", type="boolean", paramType = ApiParamType.QUERY,description = "filter by admin"),
        @ApiParam(name="user", type="boolean", paramType = ApiParamType.QUERY,description = "filter by user")
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
    @ApiMethodLight(description="List all retrieval-project for a specific project. The suggested term can use data from other project (with same ontology).", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id"),
    ])
    def listRetrieval() {
        Project project = projectService.read(params.long('id'))
        if (project) {
            responseSuccess(project.retrievalProjects)
        } else {
            responseNotFound("Project", params.id)
        }
    }

    @ApiMethodLight(description="Get the last action for a user in a project or in all projects available for the current user", listing = true)
    @ApiResponseObject(objectIdentifier="commandHistory")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id (if null: all projects)"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.QUERY,description = "The user id"),
        @ApiParam(name="fullData", type="boolean", paramType = ApiParamType.QUERY,description = "Flag to include the full JSON of the data field on each command history. Not recommended for long listing.")
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
        println request
        def result = doGenericRequest(request,fullData)
        return result
    }

    private def doGenericRequest(String request,Boolean fullData) {
        def data = []
        Long start = System.currentTimeMillis()

        new Sql(dataSource).eachRow(request) {
            if(data.isEmpty()) {
                println "TOTAL1=${System.currentTimeMillis()-start}ms"
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
        println "TOTAL2=${System.currentTimeMillis()-start}ms"
        data
    }

}

