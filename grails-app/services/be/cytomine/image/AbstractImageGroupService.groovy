package be.cytomine.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.UserGroup
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

class AbstractImageGroupService extends ModelService {

    /**
     * CRUD operation for this domain will be undo/redo-able
     */

    def cytomineService
    def transactionService

    def currentDomain() {
        return AbstractImageGroup
    }

    def get(AbstractImage abstractimage, Group group) {
        AbstractImageGroup.findByAbstractImageAndGroup(abstractimage, group)
    }

    def list(user) {
        SecurityACL.checkUser(cytomineService.currentUser)
        def groups = UserGroup.findByUser(user).collect{it.group}
        return AbstractImageGroup.findAllByGroupInList(groups)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        json.user = currentUser.id
        Command c = new AddCommand(user: currentUser)
        return executeCommand(c, null, json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AbstractImageGroup domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.abstractImage.filename, domain.group.name]
    }

    /**
      * Retrieve domain thanks to a JSON object
      * @param json JSON with new domain info
      * @return domain retrieve thanks to json
      * TODO: secure!
      */
    def retrieve(Map json) {
        AbstractImage abstractimage = AbstractImage.get(json.abstractimage)
        Group group = Group.get(json.group)
        AbstractImageGroup domain = AbstractImageGroup.findByAbstractImageAndGroup(abstractimage, group)
        if (!domain) {
            throw new ObjectNotFoundException("AbstractImageGroup group=${json.group} image=${json.abstractImage} not found")
        }
        return domain
    }

}
