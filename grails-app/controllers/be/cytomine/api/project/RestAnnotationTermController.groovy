package be.cytomine.api.project

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.UndoStackItem
import be.cytomine.project.AnnotationTerm
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.project.Annotation
import be.cytomine.project.Term

class RestAnnotationTermController {

  def springSecurityService

  def listTermByAnnotation = {
    log.info "listByAnnotation"
    if(params.idannotation && Annotation.exists(params.idannotation)) {
      def data = [:]
      data.term = Annotation.get(params.idannotation).terms()
      //data.annotationTerm = AnnotationTerm.findAllByAnnotation(Annotation.get(params.idannotation))
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Annotation Term not found with annotation id: " + params.idannotation)
        }
      }
    }
  }

  def listAnnotationByTerm = {
    log.info "listByTerm"
    if(params.idterm && Term.exists(params.idterm)) {
      def data = [:]
      data.annotation = Term.get(params.idterm).annotations()
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Annotation Term not found with term id: " + params.idterm)
        }
      }
    }
  }

  def show = {
    log.info "Show"
      Annotation annotation = Annotation.get(params.idannotation)
      Term term = Term.get(params.idterm)
    if(annotation!=null && term!=null && AnnotationTerm.findByAnnotationAndTerm(annotation,term)!=null) {
      def data = [:]
      data.annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation,term)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Annotation Term not found with annotation id " + params.idannotation + " and term id " + params.idterm)
        }
      }
    }
  }

  def add = {
    log.info "Add"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : request.JSON.toString(),user: currentUser)

    def result = addAnnotationTermCommand.execute()

    if (result.status == 201) {
      addAnnotationTermCommand.save()
      new UndoStackItem(command : addAnnotationTermCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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
    log.info "User:" + currentUser.username + " params.idannotation=" + params.idannotation

    def postData = ([annotation : params.idannotation,term :params.idterm]) as JSON
    def result = null

    Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData : postData.toString(),user: currentUser)

    result = deleteAnnotationTermCommand.execute()
    if (result.status == 204) {
      deleteAnnotationTermCommand.save()
      new UndoStackItem(command : deleteAnnotationTermCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}