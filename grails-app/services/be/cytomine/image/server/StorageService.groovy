package be.cytomine.image.server

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.command.*
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

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
                user: user
        )

        if (storage.validate()) {
            storage.save()
            permissionService.addPermission(storage,user.username,BasePermission.ADMINISTRATION)

            for (imageServer in ImageServer.findAll()) {
                imageServer.save(failOnError: true)
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
