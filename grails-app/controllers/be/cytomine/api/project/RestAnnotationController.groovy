package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Annotation
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.Transaction
import be.cytomine.command.annotation.AddAnnotationCommand
import be.cytomine.command.UndoStack
import be.cytomine.command.annotation.DeleteAnnotationCommand
import be.cytomine.command.annotation.EditAnnotationCommand
import be.cytomine.project.Image

class RestAnnotationController {

  def springSecurityService

  def list = {
    log.info "List with id image:"+params.id
    def data = [:]

    if(params.id == null) {
      data.annotation = Annotation.list()
    }
    else {
      if(Image.findById(params.id)!=null) {
        (Annotation.findAllByImage(Image.findById(params.id)))
      }
      else {
        log.error "Image Id " + params.id+ " don't exist"
        response.status = 404
        render contentType: "application/xml", {
          errors {
            message("Annotation not found with id image: " + params.id)
          }
        }
      }
    }
    withFormat {
      json { render data as JSON }
      xml { render jsonMap as XML}
    }

  }

  //return 404 when not found
  def show = {
    //testExecuteEditAnnotation()
    log.info "Show with id:" + params.id
    def data = [:]
    data.annotation = Annotation.get(params.id)
    if(data.annotation!=null)  {
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }
    else {
      log.error "Annotation Id " + params.id+ " don't exist"
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Annotation not found with id: " + params.id)
        }
      }
    }
  }

  def add = {

    log.info "Add"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addAnnotationCommand = new AddAnnotationCommand(postData : request.JSON.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(addAnnotationCommand)
    def result = addAnnotationCommand.execute()
    if (result.status == 201) {
      log.info "Save command on stack"
      addAnnotationCommand.save(flush:true)
      new UndoStack(command : addAnnotationCommand, user: currentUser).save()
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }


  def delete = {
    println "delete"

    User currentUser = User.get(springSecurityService.principal.id)
    def postData = ([id : params.id]) as JSON
    println postData.toString()
    Command deleteAnnotationCommand = new DeleteAnnotationCommand(postData : postData.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(deleteAnnotationCommand)
    def result = deleteAnnotationCommand.execute()

    if (result.status == 204) {
      deleteAnnotationCommand.save()
      new UndoStack(command : deleteAnnotationCommand, user: currentUser).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }


  def update = {
    println "update"
    User currentUser = User.get(springSecurityService.principal.id)
    println "json=" + request.JSON.toString()
    Command editAnnotationCommand = new EditAnnotationCommand(postData : request.JSON.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(editAnnotationCommand)
    def result = editAnnotationCommand.execute()

    if (result.status == 200) {
      editAnnotationCommand.save()
      new UndoStack(command : editAnnotationCommand, user: currentUser).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
