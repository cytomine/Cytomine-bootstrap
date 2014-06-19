package be.cytomine.integration

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
class AuroraController extends RestController {

    def dataSource
    def springSecurityService
    def auroraService

    def index() {}

    def retrieveAurora() {
        List<AbstractImage> imagesToNotify = auroraService.getAuroraImageNotYetNotificated()
        String request = auroraService.doRequestContent(imagesToNotify)
        responseSuccess(JSON.parse(request))
    }

    def markNotifyAurora() {
        def json = request.JSON
        auroraService.processResponse(json)
        responseSuccess([:])
    }
}
