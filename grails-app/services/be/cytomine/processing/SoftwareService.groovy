package be.cytomine.processing

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject

class SoftwareService extends ModelService {

    static transactional = true

    final boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def domainService

    def list() {
        Software.list()
    }

    def list(Project project) {
        project.softwareProjects.collect {it.software}
    }

    def read(def id) {
        Software.read(id)
    }

    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) throws CytomineException {
        log.info "update software service"
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }



    def delete(def domain,def json) throws CytomineException {
        //Start transaction
        Transaction transaction = transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        //Read software
        //Software software = Software.read(json.id)

        //TODO: Delete each Job

        //TODO: Delete each software-projects

        //TODO: Delete each software-parameters

        //Delete software
        log.info "Delete software"
        def result = executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)

        //Stop transaction
        transactionService.stop()

        return result
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Software.createFromDataWithId(json), printMessage)
    }

    def create(Software domain, boolean printMessage) {

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
        destroy(Software.get(json.id), printMessage)
    }

    def destroy(Software domain, boolean printMessage) {
        if (domain && Job.findAllBySoftware(domain).size() > 0) throw new ConstraintException("Software is still map with job")
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new Software(), json), printMessage)
    }

    def edit(Software domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Software createFromJSON(def json) {
        return Software.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Software software = Software.get(json.id)
        if (!software) throw new ObjectNotFoundException("Software " + json.id + " not found")
        return software
    }
}
