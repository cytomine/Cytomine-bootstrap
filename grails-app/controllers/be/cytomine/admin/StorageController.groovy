package be.cytomine.admin

import be.cytomine.image.server.Storage
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class StorageController {

    static scaffold = Storage
}
