package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

class UserGroupService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def transactionService

    def currentDomain() {
        UserGroup
    }

    def list(User user) {
        SecurityACL.checkUser(cytomineService.currentUser)
        UserGroup.findAllByUser(user)
    }

    def get(User user, Group group) {
        SecurityACL.checkUser(cytomineService.currentUser)
        UserGroup.findByUserAndGroup(user, group)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
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
    def delete(UserGroup domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.user, domain.group]
    }

    /**
      * Retrieve domain thanks to a JSON object
      * @param json JSON with new domain info
      * @return domain retrieve thanks to json
      */
    def retrieve(Map json) {
        User user = User.read(json.user)
        Group group = Group.read(json.group)
        UserGroup domain = UserGroup.findByUserAndGroup(user, group)
        if (!domain) throw new ObjectNotFoundException("Usergroup with user $user and group $group not found")
        return domain
    }

}
