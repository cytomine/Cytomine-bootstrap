package be.cytomine.image.server

import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE

class StorageService extends ModelService {

    def cytomineService
    def transactionService
    def permissionService

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


    def initUserStorage(SecUser user) {  //:to do => use command instead of domains

        String storage_base_path = grailsApplication.config.storage_path
        String remotePath = [storage_base_path, user.id.toString()].join(File.separator)

        log.info ("create storage for $user.username")
        Storage storage = new Storage(
                name: "$user.username storage",
                basePath: remotePath,
                ip: "10.3.1.136",
                username: "storage",
                password: "bioinfo;3u54",
                keyFile: null,
                port: 22,
                user: user
        )

        if (storage.validate()) {
            storage.save()
            permissionService.addPermission(storage,user.username,BasePermission.ADMINISTRATION)
            /*fileSystemService.makeRemoteDirectory(
                    storage.getIp(),
                    storage.getPort(),
                    storage.getUsername(),
                    storage.getPassword(),
                    storage.getKeyFile(),
                    storage.getBasePath())*/

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
