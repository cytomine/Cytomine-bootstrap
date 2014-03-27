package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.ImageFilter
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for image filter, filter that can be apply to a picture
 */
@Api(name = "image filter services", description = "Methods for managing image filter, filter that can be apply to a picture")
class RestImageFilterController extends RestController {

    def imageFilterService
    def projectService

    /**
     * List all image filter
     */
    @ApiMethodLight(description="List all image filter", listing = true)
    def list() {
        responseSuccess(imageFilterService.list())
    }

    /**
     * Get an image filter
     */
    @ApiMethodLight(description="Get an image filter")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The image filter id")
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
