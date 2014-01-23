package be.cytomine.api.image.server

import be.cytomine.api.RestController
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

@Api(name = "storage abstract image services", description = "Methods for managing the link between an image and its storage list")
class RestStorageAbstractImageController extends RestController {

    def storageAbstractImageService

    /**
     * Add a new storage to an abstract image
     */
    @ApiMethodLight(description="Add a new storage to an abstract image")
    def add() {
        add(storageAbstractImageService, request.JSON)
    }

    /**
     * Remove a group from an abstract image
     */
    @ApiMethodLight(description="Delete a storage from an abstract image list)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The abstractimage-storage id")
    ])
    def delete() {
        delete(storageAbstractImageService,[id : params.id], null)
    }
}
