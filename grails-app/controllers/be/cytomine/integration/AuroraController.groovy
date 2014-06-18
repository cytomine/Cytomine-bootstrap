package be.cytomine.integration

import be.cytomine.api.RestController
import grails.converters.JSON

class AuroraController extends RestController {

    def dataSource
    def springSecurityService
    def auroraService

    def index() {}

    def retrieveAurora() {
        String request = auroraService.doRequestContent()
        responseSuccess(JSON.parse(request))
    }

    def markNotifyAurora() {
        def json = request.JSON
        auroraService.processResponse(json)
        responseSuccess([:])
    }
}
