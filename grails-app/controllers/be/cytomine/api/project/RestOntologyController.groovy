package be.cytomine.api.project

import be.cytomine.project.Term
import grails.converters.JSON
import grails.converters.XML
import be.cytomine.project.Ontology

class RestOntologyController {

  def list = {

    println params.id
    def data = [:]
    Term term = Term.get(params.id);
    println term

    if(params.id == null) {
      data.ontology = Ontology.list()
    } else
    {
      //TODO: check if term exist
      data.ontology = Term.get(params.id).ontologies()
    }
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {

    if(params.id && Ontology.exists(params.id)) {
      def data = [:]
      data.ontology = Ontology.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Ontology not found with id: " + params.id)
        }
      }
    }
  }
}
