package be.cytomine.api.project

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStackItem

import be.cytomine.command.relationterm.AddRelationTermCommand
import be.cytomine.command.relationterm.DeleteRelationTermCommand
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term

class RestRelationTermController {

    def springSecurityService

  def list = {
    log.info "List"
      def data = RelationTerm.list()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }

  def listByRelation = {
    log.info "listByRelation"
    Relation relation = Relation.read(params.id)
    if(relation) {
      def data =RelationTerm.findAllByRelation(relation)
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Relation Term not found with relation id: " + params.id)
        }
      }
    }
  }

  def listByTerm = {
    log.info "listByTerm"
    Term term = Term.read(params.id)
    String position = params.i
    if(term && (position=="1" || position=="2")) {
      def data =  position=="1" ? RelationTerm.findAllByTerm1(term) : RelationTerm.findAllByTerm2(term)
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Relation Term not found with term" + position + " id: " + params.id)
        }
      }
    }
  }


  def show = {
    log.info "Show"
    Relation relation = Relation.get(params.idrelation)
    Term term1 = Term.get(params.idterm1)
    Term term2 = Term.get(params.idterm2)
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    if(relation && term1 && term2 && relationTerm) {
      def data = relationTerm
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Relation Term not found with relation id " + params.idrelation + ", term1 id " + params.idterm1 + " and term2 id " + params.idterm2)
        }
      }
    }
  }

    def add = {
      log.info "Add"
      User currentUser = User.get(springSecurityService.principal.id)
      log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

      Command addRelationTermCommand = new AddRelationTermCommand(postData : request.JSON.toString(),user: currentUser)

      def result = addRelationTermCommand.execute()

      if (result.status == 201) {
        addRelationTermCommand.save()
        new UndoStackItem(command : addRelationTermCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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
    def postData = ([relation : params.idrelation,term1: params.idterm1,term2: params.idterm2]) as JSON
    def result = null

    Command deleteRelationTermCommand = new DeleteRelationTermCommand(postData : postData.toString(),user: currentUser)

    result = deleteRelationTermCommand.execute()
    if (result.status == 200) {
      deleteRelationTermCommand.save()
      new UndoStackItem(command : deleteRelationTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
