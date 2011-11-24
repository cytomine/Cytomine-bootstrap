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
    def responseService

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

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(def json, String commandType, boolean printMessage) {
        //Rebuilt object that was previoulsy deleted
        def domain = Discipline.createFromDataWithId(json)
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
        def domain = Discipline.get(json.id)
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
        def domain = fillDomainWithData(new Discipline(),json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Save update
        domain.save(flush: true)
        return response
    }
}
