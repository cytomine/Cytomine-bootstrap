package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.social.LastConnection
import grails.converters.JSON
import grails.converters.XML

class ServerController {

    def springSecurityService
    def grailsApplication

    def ping = {

        def jsonContent = request.JSON
        synchronized (this.getClass()) {
            def data = [:]
                    data.alive = true
                    data.authenticated = springSecurityService.isLoggedIn()
                    data.version = grailsApplication.metadata['app.version']
                    data.serverURL = grailsApplication.config.grails.serverURL

                    Project project
                    if(!jsonContent.project.toString().equals("null"))
                        project = Project.read(Long.parseLong(jsonContent.project+""))

                    if (data.authenticated)  {
                        data.user = springSecurityService.principal.id
                        //set last ping
                        SecUser user = SecUser.get(springSecurityService.principal.id)

                        LastConnection lastConnection =  LastConnection.findByUserAndProject(user,project)
                        if(!lastConnection) {
                            lastConnection = new LastConnection(user:user,date: new Date(),project:project)
                        } else {
                            lastConnection.setDate(new Date())
                        }
                        lastConnection.save(flush:true)
                    }
            withFormat {
                json { render data as JSON }
                xml { render data as XML}
            }
        }
    }
}
