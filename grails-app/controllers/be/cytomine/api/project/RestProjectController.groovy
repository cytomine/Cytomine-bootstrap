package be.cytomine.api.project

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON

/**
 * Controller for project domain
 * A project has some images and a set of annotation
 * Users can access to project with Spring security Acl plugin
 */
class RestProjectController extends RestController {

    def springSecurityService
    def projectService
    def disciplineService
    def ontologyService
    def cytomineService
    def transactionService
    def retrievalService
    def imageInstanceService
    def securityService

    /**
     * List all project available for the current user
     */
    def list = {
        SecUser user = cytomineService.currentUser
        if(user.isAdmin()) {
            //if user is admin, we print all available project
            responseSuccess(Project.list())
        } else {
            // better perf with this direct hql request on spring security acl domain table (than post filter)
            responseSuccess(securityService.getProjectList(user))
        }
    }

    /**
     * List all project available for this user, that can use a software
     */
    def listBySoftware = {
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
    def listByOntology = {
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
    def listByUser = {
        User user = User.read(params.long('id'))
        if(user) {
            responseSuccess(projectService.list(user))
        } else {
            responseNotFound("User", params.id)
        }
    }

    /**
     * List all project available for the current user, that are link with a discipline
     */
    def listByDiscipline = {
        Discipline discipline = disciplineService.read(params.long('id'));
        if (discipline) {
            responseSuccess(projectService.list(discipline))
        } else {
            responseNotFound("Project", "Discipline", params.id)
        }
    }

    /**
     * List all retrieval-project for a specific project
     * The suggested term can use data from other project (with same ontology).
     */
    def listRetrieval = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) {
            responseSuccess(project.retrievalProjects)
        } else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * Get a project
     */
    def show = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) {
            projectService.checkAuthorization(project)
            responseSuccess(project)
        } else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * Get last action done on a specific project
     * ex: "user x add a new annotation on image y",...
     */
    def lastAction = {
        Project project = projectService.read(params.long('id'),new Project())
        int max = Integer.parseInt(params.max);

        if (project) {
            responseSuccess(projectService.lastAction(project, max))
        } else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * Add a new project to cytomine
     */
    def add = {
        add(projectService, request.JSON)
    }

    /**
     * Update a project
     */
    def update = {
        try {
            def domain = projectService.retrieve(request.JSON)
            def result = projectService.update(domain, request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    /**
     * Delete a project
     */
    def delete = {
        try {
            def domain = projectService.retrieve(JSON.parse("{id : $params.id}"))
            def result = projectService.delete(domain,JSON.parse("{id : $params.id}"))
            //delete container in retrieval
            try {retrievalService.deleteContainerAsynchronous(params.id) } catch(Exception e) {log.error e}
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }
}

