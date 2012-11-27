package be.cytomine.admin

import grails.plugins.springsecurity.Secured
import be.cytomine.image.server.ImageServer

@Secured(['ROLE_ADMIN'])
class ImageServerController {

    static scaffold = ImageServer
}
