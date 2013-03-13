package be.cytomine.api.image.server

import be.cytomine.api.RestController

class RestStorageAbstractImageController extends RestController {

    def storageAbstractImageService

    /**
     * Add a new group to an abstract image
     */
    def add = {
        add(storageAbstractImageService, request.JSON)
    }

    /**
     * Remove a group from an abstract image
     */
    def delete = {
        delete(storageAbstractImageService,[id : params.id], null)
    }
}
