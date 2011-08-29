package be.cytomine.api

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStackItem
import be.cytomine.ontology.Relation
import be.cytomine.command.relation.AddRelationCommand
import be.cytomine.command.relation.EditRelationCommand
import be.cytomine.command.relation.DeleteRelationCommand
import be.cytomine.api.RestController

class RestRelationController extends RestController {

  def springSecurityService

  def list = {
    log.info "List"
    responseSuccess(Relation.list())
  }

  def show = {
    log.info "Show id:" + params.id
    Relation relation = Relation.read(params.id)
    if(relation!=null) responseSuccess(relation)
    else responseNotFound("Relation", params.id)
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addRelationCommand = new AddRelationCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(addRelationCommand, currentUser)
    response(result)
  }

  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editRelationCommand = new EditRelationCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(editRelationCommand, currentUser)
    response(result)
  }

  def delete = {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON
    Command deleteRelationCommand = new DeleteRelationCommand(postData : postData.toString(), user: currentUser)
    def result = processCommand(deleteRelationCommand, currentUser)
    response(result)
  }

}
