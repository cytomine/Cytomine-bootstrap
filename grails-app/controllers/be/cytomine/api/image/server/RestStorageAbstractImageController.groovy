package be.cytomine.api.image.server

import be.cytomine.api.RestController
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

@RestApi(name = "storage abstract image services", description = "Methods for managing the link between an image and its storage list")
class RestStorageAbstractImageController extends RestController {

    def storageAbstractImageService

    /**
     * Add a new storage to an abstract image
     */
    @RestApiMethod(description="Add a new storage to an abstract image")
    def add() {
        add(storageAbstractImageService, request.JSON)
    }

    /**
     * Remove a group from an abstract image
     */
    @RestApiMethod(description="Delete a storage from an abstract image list)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The abstractimage-storage id")
    ])
    def delete() {
        delete(storageAbstractImageService,[id : params.id], null)
    }
}
