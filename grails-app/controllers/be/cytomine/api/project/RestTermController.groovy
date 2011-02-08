package be.cytomine.api.project

import be.cytomine.project.Term
import grails.converters.XML
import grails.converters.JSON

class RestTermController {

  def list = {
    def data = [:]
    data.term = Term.list()
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    if(params.idterm && Term.exists(params.idterm)) {
      def data = Term.findById(params.idterm)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Term not found with id: " + params.id)
        }
      }
    }
  }

}
