package be.cytomine.api.project

import be.cytomine.ontology.Term
import grails.converters.XML
import grails.converters.JSON
import be.cytomine.ontology.Annotation

import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.term.AddTermCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.command.term.EditTermCommand
import be.cytomine.command.term.DeleteTermCommand
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.ontology.Ontology
import be.cytomine.image.Image
import be.cytomine.api.RestController

class RestTermController extends RestController{

  def springSecurityService
  def transactionService

  def list = {
    responseSuccess(Term.list())
  }

  def show = {
    log.info "Show:"+ params.id
    Term term = Term.read(params.id)
    if(term) responseSuccess(term)
    else responseNotFound("Term",params.id)
  }

  def listByOntology = {
    log.info "listByOntology " + params.idontology
    Ontology ontology = Ontology.read(params.idontology)
    if(ontology) responseSuccess(ontology.terms())
    else responseNotFound("Term","Ontology",params.idontology)
  }

  def listByImage = {
    log.info "listByImage " + params.id
    Image image = Image.read(params.id)
    if(image) responseSuccess(image.terms())
    else responseNotFound("Term","Image",params.id)
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addTermCommand = new AddTermCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(addTermCommand, currentUser)
    response(result)
  }


  def delete = {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON
    Command deleteTermCommand = new DeleteTermCommand(postData : postData.toString(), user: currentUser)
    def result = processCommand(deleteTermCommand, currentUser)
    response(result)
  }


  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editTermCommand = new EditTermCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(editTermCommand, currentUser)
    response(result)
  }


}
