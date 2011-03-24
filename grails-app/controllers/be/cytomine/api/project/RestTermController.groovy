package be.cytomine.api.project

import be.cytomine.project.Term
import grails.converters.XML
import grails.converters.JSON
import be.cytomine.project.Annotation

import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.term.AddTermCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.command.term.EditTermCommand
import be.cytomine.command.term.DeleteTermCommand
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.project.Ontology
import be.cytomine.image.Image

class RestTermController {

  def springSecurityService
  def transactionService

  def list = {

    log.info "List:"+ params.id
    def data = [:]

    if(params.id == null) {
      data.term = Term.list()
    } else
    {
      if(Annotation.exists(params.id))
        data.term = Annotation.get(params.id).terms()
      else {
        response.status = 404
        render contentType: "application/xml", {
          errors {
            message("Annotation not found with id: " + params.id)
          }
        }
      }
    }
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    log.info "Show:"+ params.id
    if(params.id && Term.exists(params.id)) {
      def data = [:]
      data.term = Term.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
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
      //data.ontology = data.term = Ontology.get(params.idontology)
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

  def listTermByImage = {
    log.info "listTermByImage"
    if(params.id && Image.exists(params.id)) {
      def data = [:]
      data.term = Image.get(params.id).terms()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Image not found with id: " + params.id)
        }
      }
    }
  }

  def add = {
    log.info "Add"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addTermCommand = new AddTermCommand(postData : request.JSON.toString(),user: currentUser)

    def result = addTermCommand.execute()

    if (result.status == 201) {
      addTermCommand.save()
      new UndoStackItem(command : addTermCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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
    if((String)params.id!=(String)request.JSON.term.id) {
      log.error "Term id from URL and from data are different:"+ params.id + " vs " +  request.JSON.term.id
      result = [data : [term : null , errors : ["Term id from URL and from data are different:"+ params.id + " vs " +  request.JSON.term.id ]], status : 400]
    }
    else
    {

      Command editTermCommand = new EditTermCommand(postData : request.JSON.toString(),user: currentUser)
      result = editTermCommand.execute()

      if (result.status == 200) {
        editTermCommand.save()
        new UndoStackItem(command : editTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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

    Command deleteTermCommand = new DeleteTermCommand(postData : postData.toString(),user: currentUser)

    result = deleteTermCommand.execute()
    if (result.status == 200) {
      deleteTermCommand.save()
      new UndoStackItem(command : deleteTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def addTerm = {

    log.info "AddTerme"
    User currentUser = User.read(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : request.JSON.toString(),user: currentUser)
    def result = addAnnotationTermCommand.execute()
    if (result.status == 201) {
      addAnnotationTermCommand.save()
      new UndoStackItem(command : addAnnotationTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
