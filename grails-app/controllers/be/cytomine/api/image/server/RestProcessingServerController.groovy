package be.cytomine.api.image.server

import be.cytomine.api.RestController

class RestProcessingServerController extends RestController {

    def processingServerService

    def list = {
        response(processingServerService.list())
    }
}
