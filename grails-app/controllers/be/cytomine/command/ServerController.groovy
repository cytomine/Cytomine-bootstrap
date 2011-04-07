package be.cytomine.command

import grails.converters.JSON
import grails.converters.XML

class ServerController {

  def ping = {

    def data = [:]
    data.alive = true;

    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }
}
