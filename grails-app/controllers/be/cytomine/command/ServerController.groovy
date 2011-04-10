package be.cytomine.command

import grails.converters.JSON
import grails.converters.XML

class ServerController {

  def springSecurityService

  def ping = {

    def data = [:]
    data.alive = true
    data.authenticated = springSecurityService.isLoggedIn()
    if (data.authenticated)
      data.user = springSecurityService.principal.id

    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }
}
