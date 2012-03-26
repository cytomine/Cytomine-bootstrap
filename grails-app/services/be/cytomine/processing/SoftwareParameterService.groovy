package be.cytomine.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.security.SecUser
import be.cytomine.command.AddCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.AlreadyExistException

class SoftwareParameterService extends ModelService{

   static transactional = true

    boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def domainService
    def responseService

    def list() {
        SoftwareParameter.list()
    }

    def read(def id) {
        SoftwareParameter.read(id)
    }

    def list(Software software) {
        SoftwareParameter.findAllBySoftware(software)
    }

   def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def domain,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        def result = executeCommand(new DeleteCommand(user: currentUser), json)
        return result
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(SoftwareParameter.createFromDataWithId(json), printMessage)
    }

    def create(SoftwareParameter domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.name, domain.type, domain.software?.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(SoftwareParameter.get(json.id), printMessage)
    }

    def destroy(SoftwareParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.name, domain.type, domain.software?.name], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new SoftwareParameter(), json), printMessage)
    }

    def edit(SoftwareParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.name, domain.type, domain.software?.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    SoftwareParameter createFromJSON(def json) {
        return SoftwareParameter.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        SoftwareParameter parameter = SoftwareParameter.get(json.id)
        if (!parameter) throw new ObjectNotFoundException("SoftwareParameter " + json.id + " not found")
        return parameter
    }
}
