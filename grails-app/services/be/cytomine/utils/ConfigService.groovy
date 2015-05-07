package be.cytomine.utils

import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import grails.transaction.Transactional

@Transactional
class ConfigService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def dataSource
    def securityACLService

    def currentDomain() {
        return Config;
    }

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        return Config.list()
    }

    def readByKey(String key) {
        securityACLService.checkGuest(cytomineService.currentUser)
        Config.findByKey(key)
    }

    def add(def json) {
        securityACLService.checkAdmin(cytomineService.currentUser)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new AddCommand(user: currentUser)
        return executeCommand(command,null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Config ap, def jsonNewData) {
        securityACLService.checkAdmin(cytomineService.currentUser)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new EditCommand(user: currentUser)
        return executeCommand(command,ap,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Config domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        securityACLService.checkAdmin(cytomineService.currentUser)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.key]
    }

}
