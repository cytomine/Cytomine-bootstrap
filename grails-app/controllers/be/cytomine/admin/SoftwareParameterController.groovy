package be.cytomine.processing

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class SoftwareParameterController {

    static scaffold = SoftwareParameter
}
