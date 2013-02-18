package be.cytomine.image.server

import be.cytomine.Exception.CytomineMethodNotYetImplementedException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
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

class StorageService extends ModelService {

    def cytomineService
    def transactionService

    static transactional = true

    def currentDomain() {
        return Storage
    }

    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list() {
        //list ALL storage, order by name
        Storage.list(sort: "name")
    }

    def get(def id) {
        def storage = Storage.get((Long) id)
        if(storage) {
            storage.checkReadPermission()
        }
        storage
    }

    def read(def id) {
        def storage =  Storage.read((Long) id)
        if(storage) {
            storage.checkReadPermission()
        }
        storage
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkStorageWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkStorageDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
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
