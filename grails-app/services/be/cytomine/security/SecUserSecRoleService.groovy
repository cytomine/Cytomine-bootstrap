package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON

class SecUserSecRoleService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    def transactionService

    @PreAuthorize("hasRole('ROLE_USER')")
    def list(User user) {
        SecUserSecRole.findAllBySecUser(user)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def get(User user, SecRole role) {
        SecUserSecRole.findBySecUserAndSecRole(user, role)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(SecUserSecRole userRole, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{user: ${userRole.secUser.id}, role: ${userRole.secRole.id}}")
        return executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(SecUserSecRole.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(SecUserSecRole domain, boolean printMessage) {
        //Build response message
        log.debug "domain=" + domain + " responseService=" + responseService
        def response = responseService.createResponseMessage(domain, [domain.secUser.id, domain.secRole.id], printMessage, "Add", domain.getCallBack())
        //Save new object
        saveDomain(domain)
        return response
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(def json, boolean printMessage) {
        destroy(SecUserSecRole.createFromData(json), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(SecUserSecRole domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.secUser.id, domain.secRole.id], printMessage, "Delete", domain.getCallBack())
        //Delete new object
        domain.delete(flush: true)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    SecUserSecRole createFromJSON(def json) {
        return SecUserSecRole.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(def json) {
        SecUser user = SecUser.read(json.user)
        SecRole role = SecRole.read(json.role)
        SecUserSecRole domain = SecUserSecRole.findBySecUserAndSecRole(user, role)
        if (!domain) throw new ObjectNotFoundException("Sec user sec role not found ($user,$domain)")
        return domain
    }
}
