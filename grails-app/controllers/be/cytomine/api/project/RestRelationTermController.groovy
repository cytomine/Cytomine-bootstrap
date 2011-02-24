package be.cytomine.api.project
import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStack
import be.cytomine.project.Relation
import be.cytomine.command.relationterm.AddRelationTermCommand
import be.cytomine.command.relationterm.DeleteRelationTermCommand
import be.cytomine.project.RelationTerm

class RestRelationTermController {

    def springSecurityService

  def list = {
    log.info "List"
      def data = [:]
      data.relationTerm = RelationTerm.list()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }

    def show = {
      log.info "Show"
      if(params.id && RelationTerm.exists(params.id)) {
        def data = [:]
        data.relationTerm = RelationTerm.findById(params.id)
        withFormat {
          json { render data as JSON }
          xml { render data as XML }
        }
      } else {
        response.status = 404
        render contentType: "application/xml", {
          errors {
            message("Relation Term not found with id: " + params.id)
          }
        }
      }
    }

    def add = {
      log.info "Add"
      User currentUser = User.get(springSecurityService.principal.id)
      log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

      Command addRelationTermCommand = new AddRelationTermCommand(postData : request.JSON.toString())

      def result = addRelationTermCommand.execute()

      if (result.status == 201) {
        addRelationTermCommand.save()
        new UndoStack(command : addRelationTermCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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

    Command deleteRelationTermCommand = new DeleteRelationTermCommand(postData : postData.toString())

    result = deleteRelationTermCommand.execute()
    if (result.status == 204) {
      deleteRelationTermCommand.save()
      new UndoStack(command : deleteRelationTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
