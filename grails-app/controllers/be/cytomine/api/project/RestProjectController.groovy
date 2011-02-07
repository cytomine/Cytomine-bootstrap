package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Project
import be.cytomine.command.project.AddProjectCommand
import be.cytomine.command.Transaction
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.UndoStack
import be.cytomine.command.project.DeleteProjectCommand
import be.cytomine.command.project.EditProjectCommand

class RestProjectController {

  def springSecurityService

  def list = {
    def data = [:]
    data.project = Project.list()
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    if(params.id && Project.exists(params.id)) {
      def data = Project.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Project not found with id: " + params.id)
        }
      }
    }
  }

  def save = {
    User currentUser = User.get(springSecurityService.principal.id)
    Command addProjectCommand = new AddProjectCommand(postData : request.JSON.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(addProjectCommand)
    def result = addProjectCommand.execute()

    if (result.status == 201) {
      addProjectCommand.save()
      new UndoStack(command : addProjectCommand, user: currentUser).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  
  def update = {
    User currentUser = User.get(springSecurityService.principal.id)
    Command editProjectCommand = new EditProjectCommand(postData : request.JSON.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(editProjectCommand)
    def result = editProjectCommand.execute()

    if (result.status == 200) {
      editProjectCommand.save()
      new UndoStack(command : editProjectCommand, user: currentUser).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
  
  def delete =  {
    User currentUser = User.get(springSecurityService.principal.id)
    def postData = ([id : params.id]) as JSON
    def result = null

    Command deleteProjectCommand = new DeleteProjectCommand(postData : postData.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(deleteProjectCommand)
    result = deleteProjectCommand.execute()

    if (result.status == 204) {
      deleteProjectCommand.save()
      new UndoStack(command : deleteProjectCommand, user: currentUser).save()
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}

