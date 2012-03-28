package be.cytomine.security

import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class ServerController {

    def springSecurityService
    def grailsApplication

    def ping = {

        def data = [:]
        data.alive = true
        data.authenticated = springSecurityService.isLoggedIn()
        data.version = grailsApplication.metadata['app.version']
        data.serverURL = grailsApplication.config.grails.serverURL

        if (data.authenticated)
            data.user = springSecurityService.principal.id

        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }
}
