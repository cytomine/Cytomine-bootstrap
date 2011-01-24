package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Project

class RestProjectController {

  def list = {
    def data = [:]
    data.project = Project.list()
    if (params.format.toLowerCase() == "json") {
      render data as JSON
    } else if (params.format.toLowerCase() == "xml") {
      render data as XML
    }
  }
}
