package be.cytomine.api.project

import be.cytomine.project.Scan
import grails.converters.*


class RestScanController {

  def index = {
    redirect(controller: "scan")
  }

  def springSecurityService

  /* REST API */

  def list = {
    def data = [:]
    data.scan = Scan.list()
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    if(params.id && scan.exists(params.id)) {
      def data = scan.findById(params.id)
    } else {
      withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
      //SendNotFoundResponse()
    }

  }


}
