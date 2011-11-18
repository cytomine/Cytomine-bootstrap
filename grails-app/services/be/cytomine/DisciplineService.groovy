package be.cytomine

import be.cytomine.project.Discipline
import be.cytomine.security.User
import be.cytomine.command.discipline.AddDisciplineCommand
import be.cytomine.command.discipline.EditDisciplineCommand
import be.cytomine.command.discipline.DeleteDisciplineCommand
import grails.converters.JSON

class DisciplineService {

    static transactional = true
    def cytomineService
    def commandService

    def list() {
        Discipline.list()
    }

    def read(def id) {
        Discipline.read(id)
    }

    def get(def id) {
        Discipline.get(id)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new AddDisciplineCommand(user: currentUser), json)
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new EditDisciplineCommand(user: currentUser), json)
    }

    def delete(def id) {
        User currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id : $id}")
        commandService.processCommand( new DeleteDisciplineCommand(user: currentUser,printMessage:true), json)
    }
}
