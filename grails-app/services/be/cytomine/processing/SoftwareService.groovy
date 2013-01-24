package be.cytomine.processing

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.utils.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.Exception.WrongArgumentException
import org.springframework.security.acls.domain.BasePermission

class SoftwareService extends ModelService {

    static transactional = true

    final boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def modelService
    def aclUtilService

    def read(def id) {
        //TODO: check authorization?
        Software.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    def list() {
        Software.list()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        project.softwareProjects.collect {it.software}
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkSoftwareWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) throws CytomineException {
        log.info "update software service"
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkSoftwareDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) throws CytomineException {
        //Start transaction
        Transaction transaction = transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        //Read software

        Software software = Software.read(json.id)

        if(software && Job.countBySoftware(Software.read(json.id))>0) {
            throw new WrongArgumentException("There are job for this software, you cannot delete it!")
        }

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

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Software domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Add user creator as ontology admin
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Software.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Software domain, boolean printMessage) {
        if (domain && Job.findAllBySoftware(domain).size() > 0) throw new ConstraintException("Software is still map with job")
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Software(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(Software domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
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
