package be.cytomine.image.server


import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE

class StorageService extends ModelService {

    def cytomineService
    def transactionService
    def permissionService
    def securityACLService
    def springSecurityService

    static transactional = true

    def currentDomain() {
        return Storage
    }

    def list() {
        def list = securityACLService.getStorageList(cytomineService.currentUser)
        list.sort{it.name}
    }

    def read(def id) {
        def storage =  Storage.read((Long) id)
        if(storage) {
            securityACLService.check(storage,READ)
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
        securityACLService.checkUser(currentUser)
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
        securityACLService.check(storage,WRITE)
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
        securityACLService.check(storage.container(),WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,storage,null)
    }

    def afterAdd(Storage domain, def response) {
        log.info("Add permission on " + domain + " to " + springSecurityService.authentication.name)
        if(!domain.hasACLPermission(READ)) {
            log.info("force to put it in list")
            permissionService.addPermission(domain, cytomineService.currentUser.username, BasePermission.READ)
        }
        if(!domain.hasACLPermission(ADMINISTRATION)) {
            log.info("force to put it in list")
            permissionService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
        }
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


    def initUserStorage(SecUser user) {  //:to do => use command instead of domains
        SpringSecurityUtils.reauthenticate "admin", null
        String storage_base_path = grailsApplication.config.storage_path
        String remotePath = [storage_base_path, user.id.toString()].join(File.separator)

        log.info ("create storage for $user.username")
        Storage storage = new Storage(
                name: "$user.username storage",
                basePath: remotePath,
                ip: "localhost",
                username: "storage",
                password: "bioinfo;3u54",
                keyFile: null,
                port: 22,
                user: user
        )

        if (storage.validate()) {
            storage.save()
            permissionService.addPermission(storage,user.username,BasePermission.ADMINISTRATION)

            for (imageServer in ImageServer.findAll()) {
                ImageServerStorage imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : storage)
                imageServerStorage.save(flush : true)
            }
        } else {
            storage.errors.each {
                log.error it
            }
        }

    }

}
