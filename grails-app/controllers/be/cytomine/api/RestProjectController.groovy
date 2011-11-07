package be.cytomine.api

import grails.converters.*
import be.cytomine.project.Project
import be.cytomine.command.project.AddProjectCommand
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.project.DeleteProjectCommand
import be.cytomine.command.project.EditProjectCommand
import be.cytomine.command.CommandHistory
import be.cytomine.ontology.Ontology

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
        if(params.id!=null) {
            user = User.read(params.id)
        } else {
            user = getCurrentUser(springSecurityService.principal.id)
        }

        if(user!=null) responseSuccess(user.projects())
        else responseNotFound("User",params.id)
    }

    def show = {
        Project project = Project.read(params.id)
        if(project!=null) responseSuccess(project)
        else responseNotFound("Project", params.id)
    }

    def lastAction = {
        log.info "lastAction"
        Project project = Project.read(params.id)    //need to be filter by project
        int max =  Integer.parseInt(params.max);

        if(project!=null) {
            //def commands = CommandHistory.list(sort:"created", order:"desc", max:max);
            def commands = CommandHistory.findAllByProject(project,[sort:"created", order:"desc", max:max]);
            log.info "commands=" + commands
            //merge
            responseSuccess(commands)
        }
        else responseNotFound("Project", params.id)
    }

    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command addProjectCommand = new AddProjectCommand(postData : request.JSON.toString(),user: currentUser)
        def result = processCommand(addProjectCommand, currentUser)
        response(result)
    }

    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editProjectCommand = new EditProjectCommand(postData : request.JSON.toString(),user: currentUser)
        def result = processCommand(editProjectCommand, currentUser)
        response(result)
    }

    def delete =  {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " params.id=" + params.id
        def postData = ([id : params.id]) as JSON
        Command deleteProjectCommand = new DeleteProjectCommand(postData : postData.toString(),user: currentUser)
        def result = processCommand(deleteProjectCommand, currentUser)
        response(result)
    }


}

