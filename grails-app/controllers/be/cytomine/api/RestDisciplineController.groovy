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
        Discipline discipline = Discipline.read(params.id)
        if(discipline) responseSuccess(discipline)
        else responseNotFound("Discipline",params.id)
    }

    def add = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddDisciplineCommand(user: currentUser), json)
        response(result)
    }

    def update = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditDisciplineCommand(user: currentUser), request.JSON)
        response(result)
    }

    def delete =  {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = ([id : params.id]) as JSON
        def result = processCommand( new DeleteDisciplineCommand(user: currentUser,printMessage:true), json)
        response(result)
    }

}
