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
    responseSuccess(RelationTerm.list())
  }

  def listByRelation = {
    Relation relation
    if(params.id!=null)
      relation = Relation.read(params.id)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)
    if(relation)responseSuccess(RelationTerm.findAllByRelation(relation))
    else responseNotFound("RelationTerm","Relation",params.id)
  }

  def listByTerm = {
    Term term = Term.read(params.id)
    String position = params.i

    if(term && (position=="1" || position=="2")) {
      position=="1" ? responseSuccess(RelationTerm.findAllByTerm1(term)) : responseSuccess(RelationTerm.findAllByTerm2(term))
    }
    else responseNotFound("RelationTerm","Term"+position,params.id)
  }

  def listByTermAll = {
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
    Relation relation
    if(params.idrelation!=null)
      relation = Relation.read(params.idrelation)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)

    Term term1 = Term.read(params.idterm1)
    Term term2 = Term.read(params.idterm2)

    RelationTerm relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)

    if(relation && term1 && term2 && relationTerm)
      responseSuccess(relationTerm)
     else
      responseNotFound("RelationTerm","Relation",relation,"Term1",term1,"Term2",term2)

  }

  def add = {
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def json =  request.JSON

    Relation relation
    if(json.relation!=null)
      relation = Relation.read(params.id)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)

    json.relation = relation? relation.id : -1

    def result = processCommand(new AddRelationTermCommand(user: currentUser), json)
    response(result)
  }


  def delete =  {
    User currentUser = getCurrentUser(springSecurityService.principal.id)

    Relation relation
    if(params.idrelation!=null)
      relation = Relation.read(params.idrelation)
    else
      relation = Relation.findByName(RelationTerm.names.PARENT)

    def result = deleteRelationTerm(relation? relation.id:-1,params.idterm1,params.idterm2,currentUser)
    response(result)
  }

    def deleteRelationTerm(def idRelation, def idTerm1, def idTerm2, User currentUser) {
        return deleteRelationTerm(idRelation,idTerm1,idTerm2,currentUser,true)
    }
    def deleteRelationTerm(def idRelation, def idTerm1, def idTerm2, User currentUser, boolean printMessage) {
        def json = JSON.parse("{relation: $idRelation, term1: $idTerm1, term2: $idTerm2}")
        def result = processCommand(new DeleteRelationTermCommand(user: currentUser,printMessage: printMessage), json)
        return result
    }

    def deleteRelationTermFromTerm(Term term, User currentUser) {
        def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term,term)
        log.info "relationTerm= " +relationTerm.size()

        relationTerm.each{ relterm ->
            log.info "unlink relterm:" +relationTerm.id
            deleteRelationTerm(relterm.relation.id,relterm.term1.id,relterm.term2.id,currentUser,false)
        }
    }
}
