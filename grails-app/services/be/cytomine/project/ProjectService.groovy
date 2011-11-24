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

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(def json, String commandType, boolean printMessage) {
        //Rebuilt object that was previoulsy deleted
        def domain = Project.createFromDataWithId(json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Save new object
        domain.save(flush: true)
        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, String commandType, boolean printMessage) {
         //Get object to delete
        def domain = Project.get(json.id)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Delete object
        domain.delete(flush: true)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param commandType  command name (add/delete/...) which execute this method
     * @param printMessage  print message or not
     * @return response
     */
    def edit(def json, String commandType, boolean printMessage) {
         //Rebuilt previous state of object that was previoulsy edited
        def domain = fillDomainWithData(new Project(),json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Save update
        domain.save(flush: true)
        return response
    }
}
