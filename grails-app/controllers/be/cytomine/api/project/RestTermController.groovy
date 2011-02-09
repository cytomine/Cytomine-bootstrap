package be.cytomine.api.project

import be.cytomine.project.Term
import grails.converters.XML
import grails.converters.JSON
import be.cytomine.project.Annotation
import be.cytomine.project.AnnotationTerm

class RestTermController {

  def list = {

    println "..."
    println params.idannotation
    def data = [:]
    Annotation annotation = Annotation.get(params.idannotation);
    println annotation
    // println annotation.terms()[0].name
    if(params.idannotation == null) {
      data.term = Term.list()
    } else
    {
      //TODO: check if annotation exist
      data.term = Annotation.get(params.idannotation).terms()
    }
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
