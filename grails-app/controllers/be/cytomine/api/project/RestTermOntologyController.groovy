package be.cytomine.api.project

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStackItem

import be.cytomine.project.Term
import be.cytomine.project.Ontology
import be.cytomine.project.TermOntology
import be.cytomine.command.termOntology.AddTermOntologyCommand
import be.cytomine.command.termOntology.DeleteTermOntologyCommand
import be.cytomine.command.termOntology.EditTermOntologyCommand

class RestTermOntologyController {

  def springSecurityService

  def listOntologyByTerm = {
    log.info "listOntologyByTerm"
    if(params.idterm && Term.exists(params.idterm)) {
      def data = [:]
      data.ontology = Term.get(params.idterm).ontologies()
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

  def listTermByOntology = {
    log.info "listTermByOntology"
    if(params.idontology && Ontology.exists(params.idontology)) {
      def data = [:]
      data.term = Ontology.get(params.idontology).terms()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Ontology not found with id: " + params.idontology)
        }
      }
    }
  }

  def show = {
    log.info "Show"
      Ontology ontology = Ontology.get(params.idontology)
      Term term = Term.get(params.idterm)
      TermOntology termOntology = TermOntology.findByOntologyAndTerm(ontology,term)
    if(termOntology) {
      def data = [:]
      data.termOntology = termOntology
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Ontology-Term not found with ontology id " + params.idontology + " and term id " + params.idterm)
        }
      }
    }
  }

  def add = {
    log.info "Add"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addTermOntologyCommand = new AddTermOntologyCommand(postData : request.JSON.toString(),user: currentUser)

    def result = addTermOntologyCommand.execute()

    if (result.status == 201) {
      addTermOntologyCommand.save()
      new UndoStackItem(command : addTermOntologyCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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
    log.info "User:" + currentUser.username + " params.idterm=" + params.idterm

    def postData = ([ontology : params.idontology,term :params.idterm]) as JSON
    def result = null

    Command deleteTermOntologyCommand = new DeleteTermOntologyCommand(postData : postData.toString(),user: currentUser)

    result = deleteTermOntologyCommand.execute()
    if (result.status == 204) {
      deleteTermOntologyCommand.save()
      new UndoStackItem(command : deleteTermOntologyCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def update = {

    log.info "Update"
    User currentUser = User.read(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    def result

    if((String)params.idterm!=(String)request.JSON.termOntology.term.id && (String)params.idontology!=(String)request.JSON.termOntology.ontology.id) {
      log.error "Term id or Ontology id from URL and from data are different. Term:"+ params.idterm + " vs " +  request.JSON.termOntology.term.id + " and Ontology " + params.idontology + " vs " +  request.JSON.termOntology.ontology.id
      result = [data : [termOntology : null , errors : ["Term id or Ontology id from URL and from data are different. Term:"+ params.idterm + " vs " +  request.JSON.termOntology.term.id + " and Ontology " + params.idontology + " vs " +  request.JSON.termOntology.ontology.id ]], status : 400]
    }
    else
    {
      Command editTermOntologyCommand = new EditTermOntologyCommand(postData : request.JSON.toString(), user: currentUser)

      result = editTermOntologyCommand.execute()
      if (result.status == 200) {
        log.info "Save command on stack"
        editTermOntologyCommand.save()
        new UndoStackItem(command : editTermOntologyCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
      }
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}