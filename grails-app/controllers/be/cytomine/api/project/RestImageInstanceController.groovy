package be.cytomine.api.project

import be.cytomine.image.ImageInstance
import be.cytomine.security.User
import be.cytomine.image.AbstractImage
import grails.converters.*

import be.cytomine.command.Command

import be.cytomine.project.Project

import be.cytomine.api.RestController
import be.cytomine.command.imageinstance.AddImageInstanceCommand
import be.cytomine.command.imageinstance.EditImageInstanceCommand
import be.cytomine.command.imageinstance.DeleteImageInstanceCommand
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
class RestImageInstanceController extends RestController {

  def springSecurityService
  def transactionService

  def index = {
    redirect(controller: "image")
  }
  def list = {
    log.info "list"
    response(ImageInstance.list())
  }

  def show = {
    log.info "show " + params.id
    ImageInstance image = ImageInstance.read(params.id)
    if(image!=null) responseSuccess(image)
    else responseNotFound("ImageInstance",params.id)
  }


  def showByProjectAndImage = {
    log.info "show project: " + params.idproject + " " +  " image: " + params.idimage
    Project project = Project.read(params.idproject)
    AbstractImage image = AbstractImage.read(params.idimage)
    ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)
    if(imageInstance!=null) responseSuccess(imageInstance)
    else responseNotFound("ImageInstance","Project",params.idproject,"Image",params.idimage)
  }

  def listByUser = {
    log.info "List with id user:"+params.id
    User user = User.read(params.id)
    if(user!=null) responseSuccess(ImageInstance.findAllByUser(user))
    else responseNotFound("ImageInstance","User",params.id)
  }

  def listByImage = {
    log.info "List with id user:"+params.id
    AbstractImage image = AbstractImage.read(params.id)
    if(image!=null) responseSuccess(ImageInstance.findAllByBaseImage(image))
    else responseNotFound("ImageInstance","AbstractImage",params.id)
  }

  def listByProject = {
    log.info "List with id user:"+params.id
    Project project = Project.read(params.id)
    if(project!=null) responseSuccess(ImageInstance.findAllByProject(project))
    else responseNotFound("ImageInstance","Project",params.id)
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addImageInstanceCommand = new AddImageInstanceCommand(postData : request.JSON.toString(), user: currentUser)
    def result
    synchronized(this.getClass()) {
      result = processCommand(addImageInstanceCommand, currentUser)
    }
    response(result)
  }

  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editImageInstanceCommand = new EditImageInstanceCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(editImageInstanceCommand, currentUser)
    response(result)
  }

  def delete = {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.idproject=" + params.idproject+ " params.idimage=" + params.idimage
    Project project = Project.read(params.idproject)
    AbstractImage image = AbstractImage.read(params.idimage)
    ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)
    if(!imageInstance)
    {
      responseNotFound("ImageInstance","Project",params.idproject,"Image",params.idimage)
      return
    }
    def postData = ([id : imageInstance.id]) as JSON
    Command deleteImageInstanceCommand = new DeleteImageInstanceCommand(postData : postData.toString(), user: currentUser)
    def result
    synchronized(this.getClass()) {
      result = processCommand(deleteImageInstanceCommand, currentUser)
    }
    response(result)
  }
}
