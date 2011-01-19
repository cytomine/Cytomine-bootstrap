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
       if (params.format.toLowerCase() == "json") {
          render data as JSON
       } else if (params.format.toLowerCase() == "xml") {
          render data as XML
       }
    }

    def show = {
      if(params.id && scan.exists(params.id)) {
        def data = scan.findById(params.id)
        if (params.format.toLowerCase() == "json") {
          render data as JSON
        } else if (params.format.toLowerCase() == "xml") {
          render data as XML
        }
      } else {
        //SendNotFoundResponse()
      }

    }
}
