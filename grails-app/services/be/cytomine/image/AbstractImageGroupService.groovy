package be.cytomine.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.annotation.Secured

import be.cytomine.command.Transaction
import grails.converters.JSON
import be.cytomine.security.UserGroup

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

    @Secured(['ROLE_USER'])
    def list(user) {
        def groups = UserGroup.findByUser(user).collect{it.group}
        return AbstractImageGroup.findAllByGroupInList(groups)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @Secured(['ROLE_ADMIN'])
    def add(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @Secured(['ROLE_ADMIN'])
    def delete(def json,SecurityCheck security, Task task = null) throws CytomineException {
        Transaction transaction = transactionService.start()
        delete(retrieve(json),transaction)
    }

    def delete(AbstractImageGroup aig, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{abstractimage: ${aig.abstractImage.id},group:${aig.group.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
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
