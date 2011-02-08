package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Annotation
import be.cytomine.project.Scan
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.Transaction
import be.cytomine.command.annotation.AddAnnotationCommand
import be.cytomine.command.UndoStack
import be.cytomine.command.annotation.DeleteAnnotationCommand
import be.cytomine.command.annotation.EditAnnotationCommand

class RestAnnotationController {

  def springSecurityService

  def list = {
    println "list with id scan:"+params.idscan
    def data = [:]
    data.annotation = (params.idscan == null) ? Annotation.list() : (Annotation.findAllByScan(Scan.findById(params.idscan)))
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    //testExecuteEditAnnotation()
    println "show with id:" + params.idannotation
    def data = [:]
    data.annotation = Annotation.findById(params.idannotation)
    if(data.annotation!=null)  {
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }
    else
    {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Annotation not found with id: " + params.idannotation)
        }
      }
    }
  }


  def add = {

    println "add"
    User currentUser = User.get(3)
    println request.JSON.toString()
    Command addAnnotationCommand = new AddAnnotationCommand(postData : request.JSON.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(addAnnotationCommand)
    def result = addAnnotationCommand.execute()

    if (result.status == 201) {
      addAnnotationCommand.save()
      new UndoStack(command : addAnnotationCommand, user: currentUser).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }


  def delete = {
    println "delete"
    //springSecurityService.principal.id
    User currentUser = User.get(3)
    def postData = ([id : params.idannotation]) as JSON
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
    User currentUser = User.get(3)
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
