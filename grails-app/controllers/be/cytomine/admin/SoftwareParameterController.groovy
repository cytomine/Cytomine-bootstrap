package be.cytomine.admin

import be.cytomine.processing.SoftwareParameter
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
class SoftwareParameterController {

    static scaffold = SoftwareParameter
}
