package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject

class SecUserSecRoleService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    def list(User user) {
        SecUserSecRole.findAllBySecUser(user)
    }

    def get(User user, SecRole role) {
        SecUserSecRole.findBySecUserAndSecRole(user, role)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def domain,def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }


    def update(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def list(def sortIndex, def sortOrder, def maxRows, def currentPage, def rowOffset) {
        def secRoles = SecUserSecRole.createCriteria().list(max: maxRows, offset: rowOffset) {
            order(sortIndex, sortOrder).ignoreCase()
        }
        return secRoles
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(SecUserSecRole.createFromDataWithId(json), printMessage)
    }

    def create(SecUserSecRole domain, boolean printMessage) {
        //Build response message
        log.debug "domain=" + domain + " responseService=" + responseService
        def response = responseService.createResponseMessage(domain, [domain.user.id, domain.role.id], printMessage, "Add", domain.getCallBack())
        //Save new object
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, boolean printMessage) {
        destroy(SecUserSecRole.createFromData(json), printMessage)
    }

    def destroy(SecUserSecRole domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.id, domain.role.id], printMessage, "Delete", domain.getCallBack())
        //Delete new object
        domain.delete(flush: true)
        return response
    }

    SecUserSecRole createFromJSON(def json) {
        return SecUserSecRole.createFromData(json)
    }

    def retrieve(def json) {
        User user = User.read(json.user)
        SecRole role = SecRole.read(json.role)
        SecUserSecRole domain = SecUserSecRole.findBySecUserAndSecRole(user, role)
        if (!domain) throw new ObjectNotFoundException("Sec user sec role not found ($user,$domain)")
        return domain
    }
}
