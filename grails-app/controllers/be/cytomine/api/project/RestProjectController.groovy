package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Project
import be.cytomine.command.project.AddProjectCommand
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.UndoStackItem
import be.cytomine.command.project.DeleteProjectCommand
import be.cytomine.command.project.EditProjectCommand
import be.cytomine.api.RestController

class RestProjectController extends RestController {

  def springSecurityService

  def list = {
    responseSuccess(Project.list())
  }

  def show = {
    Project project = Project.read(params.id)
    if(project!=null) responseSuccess(project)
    else responseNotFound("Project", params.id)
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addProjectCommand = new AddProjectCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(addProjectCommand, currentUser)
    response(result)
  }

  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editProjectCommand = new EditProjectCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(editProjectCommand, currentUser)
    response(result)
  }

  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON
    Command deleteProjectCommand = new DeleteProjectCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteProjectCommand, currentUser)
    response(result)
  }
}

