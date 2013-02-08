package be.cytomine

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import be.cytomine.ontology.ReviewedAnnotation

@Secured(['ROLE_ADMIN'])
class AdminController {


    def grailsApplication

    def index() { }
}
