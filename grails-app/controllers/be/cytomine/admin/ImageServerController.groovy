package be.cytomine.admin

import be.cytomine.image.server.ImageServer
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class ImageServerController {

    static scaffold = ImageServer
}
