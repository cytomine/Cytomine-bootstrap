package be.cytomine.api.project

import be.cytomine.project.Term
import grails.converters.JSON
import grails.converters.XML
import be.cytomine.project.Ontology

class RestOntologyController {


  //TODO: add/delete/update


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

  def listOntologyByTerm = {
    log.info "listOntologyByTerm"
    if(params.idterm && Term.exists(params.idterm)) {
      def data = [:]
      data.ontology = Term.get(params.idterm).ontology
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Term not found with id: " + params.idterm)
        }
      }
    }
  }

  def tree =  {
    if(params.id && Ontology.exists(params.id)) {
      def res = []
      def data = [:]
      def ontology = Ontology.findById(params.id)
      data.id = ontology.id
      data.text = ontology.getName()
      data.checked = false

      def terms = []
      ontology.terms().each {
          def term = [:]
          term.id = it.getId()
          term.text = it.getName()
          term.checked = false
          term.leaf = false
          terms << term
      }
      data.children =  terms
      res << data
      withFormat {
        json { render res as JSON }
        xml { render res as XML }
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
