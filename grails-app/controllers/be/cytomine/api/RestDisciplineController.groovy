package be.cytomine.api

import be.cytomine.command.Command
import be.cytomine.command.TransactionController
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.command.ontology.AddOntologyCommand
import be.cytomine.command.ontology.DeleteOntologyCommand
import be.cytomine.command.ontology.EditOntologyCommand
import be.cytomine.command.relationterm.DeleteRelationTermCommand
import be.cytomine.command.term.DeleteTermCommand
import grails.converters.JSON
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.command.discipline.AddDisciplineCommand
import be.cytomine.command.discipline.EditDisciplineCommand
import be.cytomine.command.discipline.DeleteDisciplineCommand

class RestDisciplineController extends RestController {

    def springSecurityService
    def transactionService

    def list = {
        responseSuccess(Discipline.list())
    }

    def show = {
        log.info "show with id:" + params.id
        Discipline discipline = Discipline.read(params.id)
        if(discipline!=null) responseSuccess(discipline)
        else responseNotFound("Discipline",params.id)
    }

    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command addDisciplineCommand = new AddDisciplineCommand(postData : request.JSON.toString(),user: currentUser)
        def result = processCommand(addDisciplineCommand, currentUser)
        response(result)
    }

    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editDisciplineCommand = new EditDisciplineCommand(postData : request.JSON.toString(),user: currentUser)
        def result = processCommand(editDisciplineCommand, currentUser)
        response(result)
    }

    def delete =  {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " params.id=" + params.id
        def postData = ([id : params.id]) as JSON
        Command deleteDisciplineCommand = new DeleteDisciplineCommand(postData : postData.toString(),user: currentUser,printMessage:true)
        def result = processCommand(deleteDisciplineCommand, currentUser)
        response(result)
    }

}
