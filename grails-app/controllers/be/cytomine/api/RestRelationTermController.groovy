package be.cytomine.api

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStackItem

import be.cytomine.command.relationterm.AddRelationTermCommand
import be.cytomine.command.relationterm.DeleteRelationTermCommand
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import be.cytomine.api.RestController

class RestRelationTermController extends RestController {

  def springSecurityService

  def list = {
    log.info "List"
    responseSuccess(RelationTerm.list())
  }

  def listByRelation = {
    log.info "listByRelation"
    Relation relation
    if(params.id!=null)
      relation = Relation.read(params.id)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)
    log.info "Relation =" + relation.name
    if(relation)responseSuccess(RelationTerm.findAllByRelation(relation))
    else responseNotFound("RelationTerm","Relation",params.id)
  }

  def listByTerm = {
    log.info "listByTerm with term "+ params.i +" id:" + params.id
    Term term = Term.read(params.id)
    String position = params.i

    if(term && (position=="1" || position=="2")) {
      position=="1" ? responseSuccess(RelationTerm.findAllByTerm1(term)) : responseSuccess(RelationTerm.findAllByTerm2(term))
    }
    else responseNotFound("RelationTerm","Term"+position,params.id)
  }

  def listByTermAll = {
    log.info "listByTerm with term id:" + params.id
    Term term = Term.read(params.id)

    if(term) {
      def relation1 = RelationTerm.findAllByTerm1(term);
      def relation2 = RelationTerm.findAllByTerm2(term);
      def all = (relation1 << relation2).flatten();
      responseSuccess(all)
    }
    else responseNotFound("RelationTerm","Term",params.id)
  }


  def show = {
    log.info "Show with relation id:" + params.idrelation + " term1:" + params.idterm1 + " term2:" + params.idterm2
    Relation relation
    if(params.idrelation!=null)
      relation = Relation.read(params.idrelation)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)

    Term term1 = Term.read(params.idterm1)
    Term term2 = Term.read(params.idterm2)

    RelationTerm relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)

    if(relation && term1 && term2 && relationTerm) {
      responseSuccess(relationTerm)
    } else {
      responseNotFound("RelationTerm","Relation",relation,"Term1",term1,"Term2",term2)
    }
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def json =  request.JSON
    log.info "User:" + currentUser.username + " request:" + json.toString()

    Relation relation
    if(json.relation!=null)
      relation = Relation.read(params.id)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)
    json.relation = relation? relation.id : -1

    Command addRelationTermCommand = new AddRelationTermCommand(postData : json.toString(), user: currentUser)
    def result = processCommand(addRelationTermCommand, currentUser)
    response(result)
  }


  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.idrelation=" + params.idrelation

    Relation relation
    if(params.idrelation!=null)
      relation = Relation.read(params.idrelation)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)

    def postData = ([relation :relation? relation.id:-1,term1: params.idterm1,term2: params.idterm2]) as JSON
    Command deleteRelationTermCommand = new DeleteRelationTermCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteRelationTermCommand, currentUser)
    response(result)
  }

}
