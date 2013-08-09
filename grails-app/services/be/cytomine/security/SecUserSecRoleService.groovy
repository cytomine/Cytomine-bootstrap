package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.processing.Job
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.READ

class SecUserSecRoleService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    def transactionService

    def currentDomain() {
        SecUserSecRole
    }

    def list(User user) {
        SecurityACL.checkUser(cytomineService.currentUser)
        SecUserSecRole.findAllBySecUser(user)
    }

    def get(User user, SecRole role) {
        SecurityACL.checkUser(cytomineService.currentUser)
        SecUserSecRole.findBySecUserAndSecRole(user, role)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(SecUserSecRole domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        println "delete="+domain.id
        SecUserSecRole.list().each {println it.id}
        if(domain.secUser.algo()) {
            Job job = ((UserJob)domain.secUser).job
            SecurityACL.check(job?.container(),READ)
        } else {
            SecurityACL.checkAdmin(currentUser)
        }
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        def result = executeCommand(c,domain,null)
        println "result="+result
        SecUserSecRole.list().each {println it.id}
        result
    }

    def getStringParamsI18n(def domain) {
        return [domain.secUser.id, domain.secRole.id]
    }

    /**
       * Retrieve domain thanks to a JSON object
       * @param json JSON with new domain info
       * @return domain retrieve thanks to json
       */
     def retrieve(Map json) {
         SecUser user = SecUser.read(json.user)
         SecRole role = SecRole.read(json.role)
         SecUserSecRole domain = SecUserSecRole.findBySecUserAndSecRole(user, role)
         if (!domain) throw new ObjectNotFoundException("Sec user sec role not found ($user,$domain)")
         return domain
     }

}
