package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.ImageFilter

class RestImageFilterController extends RestController {

    def imageFilterService
    def projectService

    def list = {
        responseSuccess(imageFilterService.list())
    }

    def show = {
        ImageFilter imageFilter = imageFilterService.read(params.long('id'))
        if (imageFilter) responseSuccess(imageFilter)
        else responseNotFound("ImageFilter", params.id)
    }


}
