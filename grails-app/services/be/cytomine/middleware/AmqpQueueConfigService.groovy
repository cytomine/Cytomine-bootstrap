package be.cytomine.middleware

import be.cytomine.Exception.CytomineException
import be.cytomine.command.*
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

/**
 * Created by julien 
 * Date : 26/02/15
 * Time : 14:32
 */
class AmqpQueueConfigService extends ModelService{

    static transactionService = true
    boolean saveOnUndoRedoStack = true

    def securityACLService

    def currentDomain() {
        return AmqpQueueConfig
    }

    AmqpQueueConfig get(def id) {
        AmqpQueueConfig.get(id)
    }

    AmqpQueueConfig read(long id) {
        AmqpQueueConfig amqpQueueConfig = AmqpQueueConfig.read(id)

        amqpQueueConfig
    }

    AmqpQueueConfig read(String name) {
        AmqpQueueConfig amqpQueueConfig = AmqpQueueConfig.findByName(name)

        amqpQueueConfig
    }


    def list() {
        AmqpQueueConfig.list()
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
    def update(AmqpQueueConfig domain, def json) throws CytomineException {
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
    def delete(AmqpQueueConfig domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def getValueCasted(String value, String type) {
        def result

        if(value != null) {
            switch (type) {
                case "String":
                    result = value
                    break
                case "Boolean":
                    result = value.toBoolean()
                    break
                case "Number":
                    result = value.toLong()
                    break
                default:
                    result = value
            }
        }
        else
            result = null

        return result
    }

    def initAmqpQueueConfigDefaultValues() {

        if(!AmqpQueueConfig.findByName("durable")) {
            AmqpQueueConfig aqcDurable = new AmqpQueueConfig(name: "durable", defaultValue: "true", index: 20, isInMap: false, type: "Boolean")
            aqcDurable.save(failOnError: true, flush: true)
        }

        if(!AmqpQueueConfig.findByName("exclusive")) {
            AmqpQueueConfig aqcExclusive = new AmqpQueueConfig(name: "exclusive", defaultValue: "false", index: 40, isInMap: false, type: "Boolean")
            aqcExclusive.save(failOnError: true, flush: true)
        }

        if(!AmqpQueueConfig.findByName("autoDelete")) {
            AmqpQueueConfig aqcAutoDelete = new AmqpQueueConfig(name: "autoDelete", defaultValue: "false", index: 60, isInMap: false, type: "Boolean")
            aqcAutoDelete.save(failOnError: true, flush: true)
        }

        if(!AmqpQueueConfig.findByName("parametersMap")) {
            AmqpQueueConfig aqcMap = new AmqpQueueConfig(name: "parametersMap", defaultValue: null, index: 80, isInMap: false, type: "String")
            aqcMap.save(failOnError: true, flush: true)
        }
    }

}
