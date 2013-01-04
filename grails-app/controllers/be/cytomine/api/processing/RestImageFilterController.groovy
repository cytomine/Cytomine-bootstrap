package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.ImageFilter

/**
 * Controller for image filter, filter that can be apply to a picture
 */
class RestImageFilterController extends RestController {

    def imageFilterService
    def projectService

    /**
     * List all image filter
     */
    def list = {
        responseSuccess(imageFilterService.list())
    }

    /**
     * Get an image filter
     */
    def show = {
        ImageFilter imageFilter = imageFilterService.read(params.long('id'))
        if (imageFilter) {
            responseSuccess(imageFilter)
        } else {
            responseNotFound("ImageFilter", params.id)
        }
    }


}
