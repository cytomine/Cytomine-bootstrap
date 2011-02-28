package be.cytomine.api.project

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStackItem
import be.cytomine.project.Relation
import be.cytomine.command.relation.AddRelationCommand
import be.cytomine.command.relation.EditRelationCommand
import be.cytomine.command.relation.DeleteRelationCommand

class RestRelationController {

    def springSecurityService

  def list = {
    log.info "List"
      def data = [:]
      data.relation = Relation.list()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }

    def show = {
      log.info "Show"
      if(params.id && Relation.exists(params.id)) {
        def data = [:]
        data.relation = Relation.findById(params.id)
        withFormat {
          json { render data as JSON }
          xml { render data as XML }
        }
      } else {
        response.status = 404
        render contentType: "application/xml", {
          errors {
            message("Relation not found with id: " + params.id)
          }
        }
      }
    }

    def add = {
      log.info "Add"
      User currentUser = User.get(springSecurityService.principal.id)
      log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

      Command addRelationCommand = new AddRelationCommand(postData : request.JSON.toString(),user: currentUser)

      def result = addRelationCommand.execute()

      if (result.status == 201) {
        addRelationCommand.save()
        new UndoStackItem(command : addRelationCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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
    if((String)params.id!=(String)request.JSON.relation.id) {
      log.error "Relation id from URL and from data are different:"+ params.id + " vs " +  request.JSON.relation.id
      result = [data : [relation : null , errors : ["Relation id from URL and from data are different:"+ params.id + " vs " +  request.JSON.relation.id ]], status : 400]
    }
    else
    {

    Command editRelationCommand = new EditRelationCommand(postData : request.JSON.toString(),user: currentUser)
    result = editRelationCommand.execute()

    if (result.status == 200) {
      editRelationCommand.save()
      new UndoStackItem(command : editRelationCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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

    Command deleteRelationCommand = new DeleteRelationCommand(postData : postData.toString(),user: currentUser)

    result = deleteRelationCommand.execute()
    if (result.status == 204) {
      deleteRelationCommand.save()
      new UndoStackItem(command : deleteRelationCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

}
