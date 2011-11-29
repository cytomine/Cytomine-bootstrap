package be.cytomine.project

import be.cytomine.ModelService
import be.cytomine.command.discipline.AddDisciplineCommand
import be.cytomine.command.discipline.DeleteDisciplineCommand
import be.cytomine.command.discipline.EditDisciplineCommand
import be.cytomine.security.User
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.ConstraintException

class DisciplineService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def responseService
    def domainService


    boolean saveOnUndoRedoStack = true

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
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json)  {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def json)  {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }


    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(JSONObject json, String commandType, boolean printMessage) {
        restore(Discipline.createFromDataWithId(json),commandType,printMessage)
    }
    def restore(Discipline domain, String commandType, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType,domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, String commandType, boolean printMessage) {
        //Get object to delete
         destroy(Discipline.get(json.id),commandType,printMessage)
    }
    def destroy(Discipline domain, String commandType, boolean printMessage) {
        //Build response message
        if (domain && Project.findAllByDiscipline(domain).size() > 0) throw new ConstraintException("Discipline is still map with project")
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType,domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param commandType  command name (add/delete/...) which execute this method
     * @param printMessage  print message or not
     * @return response
     */
    def edit(JSONObject json, String commandType, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Discipline(),json),commandType,printMessage)
    }
    def edit(Discipline domain, String commandType, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType,domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Discipline createFromJSON(def json) {
       return Discipline.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Discipline discipline = Discipline.get(json.id)
        if(!discipline) throw new ObjectNotFoundException("Discipline " + json.id + " not found")
        return discipline
    }

}
