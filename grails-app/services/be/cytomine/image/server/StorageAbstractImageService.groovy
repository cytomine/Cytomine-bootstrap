package be.cytomine.image.server

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.image.AbstractImage
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class StorageAbstractImageService extends ModelService {

    def transactionService

    def currentDomain() {
        return StorageAbstractImage
    }

    @PreAuthorize("#security.checkStorageWrite(#json['storage']) or hasRole('ROLE_ADMIN')")
    def add(def json, SecurityCheck security) {
        executeCommand(new AddCommand(user: cytomineService.getCurrentUser() ), json)
    }

    @PreAuthorize("#security.checkStorageWrite() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        Transaction transaction = transactionService.start()
        delete(retrieve(json),transaction, true)
    }

    def delete(StorageAbstractImage sai, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${sai.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
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
