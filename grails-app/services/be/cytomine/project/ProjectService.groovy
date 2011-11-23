package be.cytomine.project

import be.cytomine.ModelService
import be.cytomine.command.CommandHistory
import be.cytomine.command.project.AddProjectCommand
import be.cytomine.command.project.DeleteProjectCommand
import be.cytomine.command.project.EditProjectCommand
import be.cytomine.ontology.Ontology
import be.cytomine.security.User

class ProjectService extends ModelService {

    static transactional = true
    def ontologyService
    def cytomineService
    def commandService

    def list() {
        Project.list(sort: "name")
    }

    def list(Ontology ontology) {
        Project.findAllByOntology(ontology)
    }

    def list(User user) {
        user.projects()
    }

    def list(Discipline discipline) {
        project.findAllByDiscipline(discipline)
    }

    def read(def id) {
        Project.read(id)
    }

    def get(def id) {
        Project.get(id)
    }

    def lastAction(Project project, def max) {
        return CommandHistory.findAllByProject(project, [sort: "created", order: "desc", max: max])
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new AddProjectCommand(user: currentUser), json)
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new EditProjectCommand(user: currentUser), json)
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new DeleteProjectCommand(user: currentUser, printMessage: true), json)
    }
}
