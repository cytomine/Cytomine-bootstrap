package be.cytomine.project

import be.cytomine.ModelService
import be.cytomine.command.discipline.AddDisciplineCommand
import be.cytomine.command.discipline.DeleteDisciplineCommand
import be.cytomine.command.discipline.EditDisciplineCommand
import be.cytomine.security.User

class DisciplineService extends ModelService {

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

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new DeleteDisciplineCommand(user: currentUser, printMessage: true), json)
    }
}
