package be.cytomine.api.image.server

import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.Mime
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.Storage
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON

class RestStorageController extends RestController {

    def cytomineService
    def storageService
    def secUserService

    /**
     * List all project available for the current user
     */
    def list = {
        log.info 'listing storages'
        responseSuccess(storageService.list())
    }

    def listByMime = {
        log.info 'listing storages by mime/user'
        def currentUser = cytomineService.currentUser
        String ext = params.get('ext')

        Mime mime = Mime.findByExtension(ext)
        //list all images server for this mime
        List<ImageServer> servers = MimeImageServer.findAllByMime(mime).collect{it.imageServer}

        //list all storage for this user
        List<Storage> storages = Storage.findAllByUser(currentUser)

        if(servers.isEmpty()) {
            responseNotFound("ImageServer", "No image server found for ext=$ext")
        } else if(storages.isEmpty()) {
            responseNotFound("Storage", "No storage for user=${currentUser.id}")
        } else {
            def serverAvailableForUser = ImageServerStorage.findAllByImageServerInListAndStorageInList(servers,storages)
            if(serverAvailableForUser.isEmpty()) {
                responseNotFound("ImageServerStorage", "No imageserver-storage found for servers=${servers} and storages=${storages}")
            } else {
                responseSuccess(serverAvailableForUser)
            }
        }
    }

    /**
     * Get a project
     */
    def show = {
        Storage storage = storageService.read(params.long('id'))
        if (storage) {
            responseSuccess(storage)
        } else {
            responseNotFound("Storage", params.id)
        }
    }

    /**
     * Add a new storage to cytomine
     */
    def add = {
        add(storageService, request.JSON)
    }

    /**
     * Update a storage
     */
    def update = {
        try {
            def domain = storageService.retrieve(request.JSON)
            def result = storageService.update(domain,request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    /**
     * Delete a storage
     */
    def delete = {
        try {
            def domain = storageService.retrieve(JSON.parse("{id : $params.id}"))
            def result = storageService.delete(domain,transactionService.start(),null,true)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Create a storage for user with default parameters
     */
    def create = {
        println params
        def id = params.long('user')

        println ">>> < user id : $id"
        SecUser user = secUserService.read(id)
        if (user instanceof User) {
            if (Storage.findByUser(user)) {
                new AlreadyExistException("A storage already exists for user $user.username")
            } else {
                storageService.initUserStorage((User)user)
                responseSuccess(Storage.findByUser(user))
            }
        }
    }
}
