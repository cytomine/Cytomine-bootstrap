package be.cytomine.image.server

import be.cytomine.Exception.CytomineMethodNotYetImplementedException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.UserGroup
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import static org.springframework.security.acls.domain.BasePermission.*

class StorageService extends ModelService {

    def cytomineService
    def transactionService

    static transactional = true

    def currentDomain() {
        return Storage
    }

    def list() {
        def list = SecurityACL.getStorageList(cytomineService.currentUser)
        list.sort{it.name}
    }

    def get(def id) {
        def storage = Storage.get((Long) id)
        if(storage) {
            SecurityACL.check(storage,READ)
        }
        storage
    }

    def read(def id) {
        def storage =  Storage.read((Long) id)
        if(storage) {
            SecurityACL.check(storage,READ)
        }
        storage
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data)
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkUser(currentUser)
        Command c = new AddCommand(user: currentUser)
        executeCommand(c,null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Storage storage,def jsonNewData) {
        SecurityACL.check(storage,WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new EditCommand(user: currentUser)
        executeCommand(c,storage,jsonNewData)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    def delete(Storage storage, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecurityACL.check(storage.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,storage,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

   void deleteDependentImageServerStorage(Storage storage, Transaction transaction, Task task = null){
       //throw new CytomineMethodNotYetImplementedException("cannot yet delete imageServerStorage, implement service first!")
   }
   void  deleteDependentStorageAbstractImage(Storage storage, Transaction transaction, Task task = null) {
       //throw new CytomineMethodNotYetImplementedException("cannot yet delete StorageAbstractImage, implement service first!")

   }

}
