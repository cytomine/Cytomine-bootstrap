package be.cytomine.middleware

import be.cytomine.Exception.CytomineException
import be.cytomine.command.*
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

/**
 * Created by julien 
 * Date : 03/03/15
 * Time : 11:25
 */
class AmqpQueueConfigInstanceService extends ModelService {

    static transactionService = true
    boolean saveOnUndoRedoStack = true

    def securityACLService

    def currentDomain() {
        return AmqpQueueConfigInstance
    }

    AmqpQueueConfigInstance get(def id) {
        AmqpQueueConfigInstance.get(id)
    }

    AmqpQueueConfigInstance read(def id) {
        AmqpQueueConfigInstance amqpQueueConfigInstance = AmqpQueueConfigInstance.read(id)

        amqpQueueConfigInstance
    }

    def list() {
        AmqpQueueConfigInstance.list()
    }

    def list(AmqpQueue amqpQueue) {
        AmqpQueueConfigInstance.findAllByQueue(amqpQueue)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        return executeCommand(new AddCommand(user: currentUser), null, json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain data
     * @return Response structure (new domain data, old domain data..)
     */
    def update(AmqpQueueConfigInstance domain, def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        return executeCommand(new EditCommand(user: currentUser), domain, json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AmqpQueueConfigInstance domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.queue, domain.config, domain.value]
    }

}
