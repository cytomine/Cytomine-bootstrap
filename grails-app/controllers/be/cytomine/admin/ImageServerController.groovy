package be.cytomine.admin

import be.cytomine.image.server.ImageServer
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN'])
class ImageServerController {

    static scaffold = ImageServer
}
