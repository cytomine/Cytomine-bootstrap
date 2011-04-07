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

class RestOntologyController {

  def springSecurityService
  def transactionService
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

  }

  def add = {
    log.info "Add"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addOntologyCommand = new AddOntologyCommand(postData : request.JSON.toString(),user: currentUser)

    def result = addOntologyCommand.execute()

    if (result.status == 201) {
      addOntologyCommand.save()
      new UndoStackItem(command : addOntologyCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def update = {
    log.info "Update"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    def result
    if((String)params.id!=(String)request.JSON.ontology.id) {
      log.error "Ontology id from URL and from data are different:"+ params.id + " vs " +  request.JSON.ontology.id
      result = [data : [ontology : null , errors : ["Ontology id from URL and from data are different:"+ params.id + " vs " +  request.JSON.ontology.id ]], status : 400]
    }
    else
    {

      Command editOntologyCommand = new EditOntologyCommand(postData : request.JSON.toString(),user: currentUser)
      result = editOntologyCommand.execute()

      if (result.status == 200) {
        editOntologyCommand.save()
        new UndoStackItem(command : editOntologyCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
      }
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def delete =  {
    log.info "Delete"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON
    def result = null

    Command deleteOntologyCommand = new DeleteOntologyCommand(postData : postData.toString(),user: currentUser)

    result = deleteOntologyCommand.execute()
    if (result.status == 200) {
      deleteOntologyCommand.save()
      new UndoStackItem(command : deleteOntologyCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
