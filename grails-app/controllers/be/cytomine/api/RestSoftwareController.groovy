package be.cytomine.api

import be.cytomine.processing.Software
import grails.plugins.springsecurity.Secured

class RestSoftwareController extends RestController {

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(Software.list())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Software software = Software.read(params.id)
        if (software) responseSuccess(software)
        else responseNotFound("Software", params.id)
    }
}
