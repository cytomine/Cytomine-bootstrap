package be.cytomine.api

import be.cytomine.processing.ImageFilter

class RestImageFilterController extends RestController {

    def list = {
        responseSuccess(ImageFilter.list())
    }

    def show = {
        ImageFilter imageFilter = ImageFilter.read(params.id)
        if (imageFilter) responseSuccess(imageFilter)
        else responseNotFound("ImageFilter", params.id)
    }


}
