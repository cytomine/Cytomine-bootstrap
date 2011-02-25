package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Project
import be.cytomine.command.project.AddProjectCommand
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.UndoStackItem
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
      def data = [:]
      data.project = Project.findById(params.id)
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
    log.info "Add"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addProjectCommand = new AddProjectCommand(postData : request.JSON.toString())

    def result = addProjectCommand.execute()

    if (result.status == 201) {
      addProjectCommand.save()
      new UndoStackItem(command : addProjectCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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
    if((String)params.id!=(String)request.JSON.project.id) {
      log.error "Project id from URL and from data are different:"+ params.id + " vs " +  request.JSON.project.id
      result = [data : [project : null , errors : ["Project id from URL and from data are different:"+ params.id + " vs " +  request.JSON.project.id ]], status : 400]
    }
    else
    {

    Command editProjectCommand = new EditProjectCommand(postData : request.JSON.toString())
    result = editProjectCommand.execute()

    if (result.status == 200) {
      editProjectCommand.save()
      new UndoStackItem(command : editProjectCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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

    Command deleteProjectCommand = new DeleteProjectCommand(postData : postData.toString())

    result = deleteProjectCommand.execute()
    if (result.status == 204) {
      deleteProjectCommand.save()
      new UndoStackItem(command : deleteProjectCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}

