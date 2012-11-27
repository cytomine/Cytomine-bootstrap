package be.cytomine.admin

import grails.plugins.springsecurity.Secured
import be.cytomine.image.server.Storage

@Secured(['ROLE_ADMIN'])
class StorageController {

    static scaffold = Storage
}
