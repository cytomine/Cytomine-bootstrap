package be.cytomine.api.project

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.api.RestController
import be.cytomine.project.Project
import be.cytomine.project.Slide
import be.cytomine.project.ProjectSlide
import be.cytomine.command.projectslide.AddProjectSlideCommand
import be.cytomine.command.projectslide.DeleteProjectSlideCommand

class RestProjectSlideController extends RestController {

  def springSecurityService

  def listSlideByProject = {
    log.info "listByProject with idProject=" + params.idproject
    if(params.idproject=="undefined") responseNotFound("Project Slide","Project", params.idproject)
    else
    {
      Project project =  Project.read(params.idproject)
      if(project!=null) responseSuccess(project.slides())
      else responseNotFound("Project Slide","Project", params.idproject)
    }

  }

  def listProjectBySlide = {
    log.info "listBySlide with idSlide=" +  params.idslide
    Slide slide = Slide.read(params.idslide)
    if(slide!=null) responseSuccess(slide.projects())
    else responseNotFound("Project Slide","Slide", params.idslide)
  }

  def show = {
    log.info "listBySlide with idSlide=" +  params.idslide + " idProject=" + params.idproject
    Project project = Project.read(params.idproject)
    Slide slide = Slide.read(params.idslide)
    if(project!=null && slide!=null && ProjectSlide.findByProjectAndSlide(project,slide)!=null)
      responseSuccess(ProjectSlide.findByProjectAndSlide(project,slide))
    else  responseNotFound("Project Slide","Slide","Project", params.idslide,  params.idproject)
  }


  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addProjectSlideCommand = new AddProjectSlideCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(addProjectSlideCommand, currentUser)
    response(result)
  }

  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.idproject=" + params.idproject
    def postData = ([project : params.idproject,slide :params.idslide]) as JSON
    Command deleteProjectSlideCommand = new DeleteProjectSlideCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteProjectSlideCommand, currentUser)
    response(result)
  }
}