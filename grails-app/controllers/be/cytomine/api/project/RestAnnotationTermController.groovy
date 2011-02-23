package be.cytomine.api.project
import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStack
import be.cytomine.project.AnnotationTerm
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand

class RestAnnotationTermController {

    def springSecurityService

  def list = {
    log.info "List"
      def data = [:]
      data.annotationTerm = AnnotationTerm.list()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }

    def show = {
      log.info "Show"
      if(params.id && AnnotationTerm.exists(params.id)) {
        def data = [:]
        data.annotationTerm = AnnotationTerm.findById(params.id)
        withFormat {
          json { render data as JSON }
          xml { render data as XML }
        }
      } else {
        response.status = 404
        render contentType: "application/xml", {
          errors {
            message("Annotation Term not found with id: " + params.id)
          }
        }
      }
    }

    def add = {
      log.info "Add"
      User currentUser = User.get(springSecurityService.principal.id)
      log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

      Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : request.JSON.toString())

      def result = addAnnotationTermCommand.execute()

      if (result.status == 201) {
        addAnnotationTermCommand.save()
        new UndoStack(command : addAnnotationTermCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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

    Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData : postData.toString())

    result = deleteAnnotationTermCommand.execute()
    if (result.status == 204) {
      deleteAnnotationTermCommand.save()
      new UndoStack(command : deleteAnnotationTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}