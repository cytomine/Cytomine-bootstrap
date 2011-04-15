package be.cytomine.api.project

import be.cytomine.ontology.Term
import grails.converters.JSON
import grails.converters.XML
import be.cytomine.ontology.Ontology
import be.cytomine.security.User
import be.cytomine.command.UndoStackItem
import be.cytomine.command.Command
import be.cytomine.command.ontology.EditOntologyCommand
import be.cytomine.command.ontology.DeleteOntologyCommand
import be.cytomine.command.ontology.AddOntologyCommand
import be.cytomine.api.RestController

class RestOntologyController extends RestController {

  def springSecurityService
  def transactionService

  def list = {
    responseSuccess(Ontology.list())
  }

  def listByTerm = {
    log.info "listByTerm with term id:" + params.id
    Term term = Term.read(params.id);

    if(term != null) responseSuccess(term.ontology)
    else responseNotFound("Ontology","Term",params.id)
  }

  def show = {
    log.info "show with id:" + params.id
    Ontology ontology = Ontology.read(params.id)
    if(ontology!=null) responseSuccess(ontology)
    else responseNotFound("Ontology",params.id)
  }


  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addOntologyCommand = new AddOntologyCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(addOntologyCommand, currentUser)
    response(result)
  }

  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editOntologyCommand = new EditOntologyCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(editOntologyCommand, currentUser)
    response(result)
  }

  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON
    Command deleteOntologyCommand = new DeleteOntologyCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteOntologyCommand, currentUser)
    response(result)
  }
/*  def tree =  {
    if(params.id && Ontology.exists(params.id)) {
      def res = []
      def data = [:]
      def ontology = Ontology.findById(params.id)
      data.id = ontology.id
      data.text = ontology.getName()
      data.data = ontology.getName()
      data.checked = false

      def terms = []
      ontology.terms().each {
          def term = [:]
          term.id = it.getId()
          term.text = it.getName()
          term.data = it.getName()
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

  }*/


}
