package be.cytomine.admin

import be.cytomine.processing.SoftwareParameter
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class SoftwareParameterController {

    static scaffold = SoftwareParameter
}
