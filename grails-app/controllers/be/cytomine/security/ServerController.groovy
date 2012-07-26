package be.cytomine.security

import grails.converters.JSON
import grails.converters.XML

class ServerController {

    def springSecurityService
    def grailsApplication

    def ping = {


        def data = [:]
        data.alive = true
        data.authenticated = springSecurityService.isLoggedIn()
        data.version = grailsApplication.metadata['app.version']
        data.serverURL = grailsApplication.config.grails.serverURL

        if (data.authenticated)  {
            data.user = springSecurityService.principal.id
            //set last ping
            SecUser user = SecUser.get(springSecurityService.principal.id)
            user.setLastPing(new Date())
            user.save()
        }

        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }
}
