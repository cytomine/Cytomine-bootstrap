package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.ImageFilter
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for image filter, filter that can be apply to a picture
 */
@RestApi(name = "image filter services", description = "Methods for managing image filter, filter that can be apply to a picture")
class RestImageFilterController extends RestController {

    def imageFilterService
    def projectService

    /**
     * List all image filter
     */
    @RestApiMethod(description="List all image filter", listing = true)
    def list() {
        responseSuccess(imageFilterService.list())
    }

    /**
     * Get an image filter
     */
    @RestApiMethod(description="Get an image filter")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image filter id")
    ])
    def show() {
        ImageFilter imageFilter = imageFilterService.read(params.long('id'))
        if (imageFilter) {
            responseSuccess(imageFilter)
        } else {
            responseNotFound("ImageFilter", params.id)
        }
    }


}
