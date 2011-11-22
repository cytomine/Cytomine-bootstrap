package be.cytomine.api

import be.cytomine.processing.Software
import grails.plugins.springsecurity.Secured

class RestSoftwareController extends RestController {

    def softwareService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(softwareService.list())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Software software = softwareService.read(params.id)
        if (software) responseSuccess(software)
        else responseNotFound("Software", params.id)
    }
}
