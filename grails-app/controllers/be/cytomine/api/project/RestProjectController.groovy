package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Project

class RestProjectController {

  def list = {
    def data = [:]
    data.project = Project.list()
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }
}
