package be.cytomine.project

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject

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
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def domain,def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Discipline.createFromDataWithId(json), printMessage)
    }

    def create(Discipline domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Discipline.get(json.id), printMessage)
    }

    def destroy(Discipline domain, boolean printMessage) {
        //Build response message
        if (domain && Project.findAllByDiscipline(domain).size() > 0) throw new ConstraintException("Discipline is still map with project")
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Discipline(), json), printMessage)
    }

    def edit(Discipline domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
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
        if (!discipline) throw new ObjectNotFoundException("Discipline " + json.id + " not found")
        return discipline
    }

}
