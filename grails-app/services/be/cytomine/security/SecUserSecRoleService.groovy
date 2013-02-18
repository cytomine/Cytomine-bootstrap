package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
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

    def currentDomain() {
        SecUserSecRole
    }

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
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(SecUserSecRole userRole, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{user: ${userRole.secUser.id}, role: ${userRole.secRole.id}}")
        return executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.secUser.id, domain.secRole.id]
    }

    /**
       * Retrieve domain thanks to a JSON object
       * @param json JSON with new domain info
       * @return domain retrieve thanks to json
       */
     def retrieve(JSONObject json) {
         SecUser user = SecUser.read(json.user)
         SecRole role = SecRole.read(json.role)
         SecUserSecRole domain = SecUserSecRole.findBySecUserAndSecRole(user, role)
         if (!domain) throw new ObjectNotFoundException("Sec user sec role not found ($user,$domain)")
         return domain
     }

}
