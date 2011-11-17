package be.cytomine.api

import grails.converters.*
import be.cytomine.project.Project
import be.cytomine.command.project.AddProjectCommand
import be.cytomine.security.User
import be.cytomine.command.project.DeleteProjectCommand
import be.cytomine.command.project.EditProjectCommand
import be.cytomine.command.CommandHistory
import be.cytomine.ontology.Ontology
import be.cytomine.project.Discipline

class RestProjectController extends RestController {

    def springSecurityService

    def list = {
        responseSuccess(Project.list(sort:"name"))
    }

    def listByOntology = {
        log.info "listByOntology with ontology id:" + params.id
        Ontology ontology = Ontology.read(params.id);
        if(ontology != null) responseSuccess(Project.findAllByOntology(ontology))
        else responseNotFound("Project","Ontology",params.id)
    }

    def listByUser = {
        log.info "List with id user:"+params.id + " (null will be currentuser)"
        User user=null
        if(params.id != null)
            user = User.read(params.id)
        else
            user = getCurrentUser(springSecurityService.principal.id)

        if(user) responseSuccess(user.projects())
        else responseNotFound("User",params.id)
    }

    def listByDiscipline = {
        log.info "listByDiscipline with discipline id:" + params.id
        Discipline discipline = Discipline.read(params.id);
        if(discipline) responseSuccess(project.findAllByDiscipline(discipline))
        else responseNotFound("Project","Discipline",params.id)
    }

    def show = {
        Project project = Project.read(params.id)
        if(project) responseSuccess(project)
        else responseNotFound("Project", params.id)
    }

    def lastAction = {
        log.info "lastAction"
        Project project = Project.read(params.id)    //need to be filter by project
        int max =  Integer.parseInt(params.max);

        if(project) {
            responseSuccess(CommandHistory.findAllByProject(project,[sort:"created", order:"desc", max:max]))
        }
        else responseNotFound("Project", params.id)
    }

    def add = {
        log.info "Add"
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + json.toString()
        def result = processCommand(new AddProjectCommand(user: currentUser), json)
        response(result)
    }

    def update = {
        log.info "Update"
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        def result = processCommand(new EditProjectCommand(user: currentUser), json)
        response(result)
    }

    def delete =  {
        log.info "Delete"
        def json = ([id : params.id]) as JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " params.id=" + params.id
        def result = processCommand(new DeleteProjectCommand(user: currentUser), json)
        response(result)
    }
}

