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
    responseSuccess(Relation.list())
  }

  def show = {
    Relation relation = Relation.read(params.id)
    if(relation!=null) responseSuccess(relation)
    else responseNotFound("Relation", params.id)
  }

  def add = {
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def result = processCommand(new AddRelationCommand(user: currentUser), request.JSON)
    response(result)
  }

  def update = {
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def result = processCommand(new EditRelationCommand(user: currentUser), request.JSON)
    response(result)
  }

  def delete = {
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def json = JSON.parse("{id : $params.id}")
    def result = processCommand(new DeleteRelationCommand(user: currentUser), json)
    response(result)
  }

}
