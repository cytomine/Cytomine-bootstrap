package be.cytomine.admin

import grails.plugins.springsecurity.Secured
import be.cytomine.processing.SoftwareParameter

@Secured(['ROLE_ADMIN'])
class SoftwareParameterController {

    static scaffold = SoftwareParameter
}
