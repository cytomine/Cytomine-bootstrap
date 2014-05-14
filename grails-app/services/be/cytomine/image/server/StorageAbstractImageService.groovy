package be.cytomine.image.server


import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.WRITE

class StorageAbstractImageService extends ModelService {

    def transactionService
    def securityACLService

    def currentDomain() {
        return StorageAbstractImage
    }

    def add(def json) {
        securityACLService.check(json.storage,Storage,WRITE)
        Command c = new AddCommand(user: cytomineService.getCurrentUser())
        executeCommand(c,null,json)
    }

    def delete(StorageAbstractImage sai, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        securityACLService.check(sai.container(),WRITE)
        Command c = new DeleteCommand(user: cytomineService.getCurrentUser(),transaction:transaction)
        return executeCommand(c,sai,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.abstractImage.filename, domain.storage.name]
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     * TODO: secure!
     */
    def retrieve(Map json) {
        StorageAbstractImage.read(json.id)
    }
}
