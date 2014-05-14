package be.cytomine.security

import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException

import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.processing.Job
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON

import static org.springframework.security.acls.domain.BasePermission.READ

class SecUserSecRoleService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService
    def securityACLService

    def transactionService

    def currentDomain() {
        SecUserSecRole
    }

    def list(User user) {
        securityACLService.checkGuest(cytomineService.currentUser)
        SecUserSecRole.findAllBySecUser(user)
    }

    def get(User user, SecRole role) {
        securityACLService.checkGuest(cytomineService.currentUser)
        SecUserSecRole.findBySecUserAndSecRole(user, role)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        def command = executeCommand(new AddCommand(user: currentUser),null,json)
        return command
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
        if(domain.secUser.id==currentUser.id && !domain.secRole.authority.equals("ROLE_SUPER_ADMIN")) {
            throw new ForbiddenException("You cannot remove you a role")
        }
        if(domain.secUser.algo()) {
            Job job = ((UserJob)domain.secUser).job
            securityACLService.check(job?.container(),READ)
        } else {
            securityACLService.checkAdmin(currentUser)
        }

        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        def result = executeCommand(c,domain,null)
        result
    }

    def getStringParamsI18n(def domain) {
        return [domain.secUser.id, domain.secRole.id]
    }

    /**
     * Define a role for a user. If admin is defined, user will have admin,user,guest. If user is defined, user will have user,guest, etc
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def define(SecUser user, SecRole role) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)

        SecRole roleGuest = SecRole.findByAuthority("ROLE_GUEST")
        SecRole roleUser = SecRole.findByAuthority("ROLE_USER")
        SecRole roleAdmin = SecRole.findByAuthority("ROLE_ADMIN")
        SecRole roleSuperAdmin = SecRole.findByAuthority("ROLE_SUPER_ADMIN")

        if(role.authority.equals("ROLE_SUPER_ADMIN")) {
            addRole(user,roleGuest)
            addRole(user,roleUser)
            addRole(user,roleAdmin)
            addRole(user,roleSuperAdmin)
        } else if(role.authority.equals("ROLE_ADMIN")) {
            addRole(user,roleGuest)
            addRole(user,roleUser)
            addRole(user,roleAdmin)
            removeRole(user,roleSuperAdmin)
        } else if(role.authority.equals("ROLE_USER")) {
            addRole(user,roleGuest)
            addRole(user,roleUser)
            removeRole(user,roleAdmin)
            removeRole(user,roleSuperAdmin)
        }else if(role.authority.equals("ROLE_GUEST")) {
            addRole(user,roleGuest)
            removeRole(user,roleUser)
            removeRole(user,roleAdmin)
            removeRole(user,roleSuperAdmin)
        }
    }

    private def addRole(SecUser user,SecRole role) {
        SecUserSecRole linked = SecUserSecRole.findBySecUserAndSecRole(user,role)
        if(!linked) {
            SecUserSecRole susr = new SecUserSecRole(secUser: user,secRole:role)
            super.saveDomain(susr)
        }
    }
    private def removeRole(SecUser user,SecRole role) {
        SecUserSecRole linked = SecUserSecRole.findBySecUserAndSecRole(user,role)
        if(linked) {
            if(user.id==cytomineService.getCurrentUser().id && !role.authority.equals("ROLE_SUPER_ADMIN")) {
                throw new ForbiddenException("You cannot remove you a role")
            }
            super.removeDomain(linked)
        }
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
