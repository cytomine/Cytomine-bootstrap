package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.ProcessingServer

/**
 * TODO:: comment this controller. Explain the "processing server goal"
 */
class RestProcessingServerController extends RestController {

    def processingServerService

    def list = {
        response(processingServerService.list())
    }

    def show = {
        ProcessingServer processingServer = processingServerService.read(params.long('id'))
        if (processingServer) {
            responseSuccess(processingServer)
        } else {
            responseNotFound("ProcessingServer", params.id)
        }
    }
}
