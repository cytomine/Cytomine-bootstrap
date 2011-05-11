package be.cytomine.api.project

import be.cytomine.image.Image
import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.image.server.RetrievalServer
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.image.AddImageCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.project.Project
import be.cytomine.command.image.EditImageCommand
import be.cytomine.command.image.DeleteImageCommand
import be.cytomine.api.RestController

class RestImageController extends RestController{

  def springSecurityService
  def transactionService

  def index = {
    redirect(controller: "image")
  }
  def list = {
    log.info "list"
    response(Image.list())
  }

  def show = {
    log.info "show " + params.id
    Image image = Image.read(params.id)
    if(image!=null) responseSuccess(image)
    else responseNotFound("Image",params.id)
  }

  def listByUser = {
    log.info "List with id user:"+params.id
    User user = User.read(params.id)
    if(user!=null) responseSuccess(Image.findAllByUser(user))
    else responseNotFound("Image","User",params.id)
  }

  def listByProject = {
    log.info "List with id user:"+params.id
    Project project = Project.read(params.id)
    if(project!=null) responseSuccess(project.images())
    else responseNotFound("Image","Project",params.id)
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addImageCommand = new AddImageCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(addImageCommand, currentUser)
    response(result)
  }

  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editImageCommand = new EditImageCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(editImageCommand, currentUser)
    response(result)
  }

  def delete = {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON
    Command deleteImageCommand = new DeleteImageCommand(postData : postData.toString(), user: currentUser)
    def result = processCommand(deleteImageCommand, currentUser)
    response(result)
  }

  def metadata = {
    //TODO; refactor me!
    Image image = Image.read(params.id)
    def url = new URL(image.getMetadataURL())
    withFormat {
      json {
        render(contentType: "application/json", text: "${url.text}")
      }
    }
  }

  def thumb = {
    log.info "Thumb with id:" + params.id
    Image image = Image.read(params.id)
    log.info "image.getThumbURL()="+image.getThumbURL()
    try {
      responseImage(image.getThumbURL())
    } catch ( Exception e) {
      log.error("GetThumb:"+e);
    }
  }

  def crop = {
    log.info "Crop with id annotation: " + params.id
    Annotation annotation = Annotation.read(params.id)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle

    if(annotation==null)
      responseNotFound("Crop","Annotation",params.id)
    else if(zoom < annotation.getImage().getZoomLevels().min || zoom > annotation.getImage().getZoomLevels().max)
      responseNotFound("Crop","Zoom",zoom)
    else
    {
      try {
        if(params.zoom!=null)
          responseImage(annotation.getCropURL(zoom))
        else
          responseImage(annotation.getCropURL(null))
      } catch ( Exception e) {
        log.error("GetThumb:"+e);
      }
    }
  }

  def retrieval = {
    Annotation annotation = Annotation.read(params.idannotation)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle

    if(annotation==null)
      responseNotFound("Crop","Annotation",params.id)
    else if(zoom < annotation.getImage().getZoomLevels().min || zoom > annotation.getImage().getZoomLevels().max)
      responseNotFound("Crop","Zoom",zoom)
    else {

      int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)
      def retrievalServers = RetrievalServer.findAll()
      log.debug "annotation.getCropURL(1)=" + annotation.getCropURL(1)
      def list = retrievalServers.get(0).search(annotation.getCropURL(zoom), maxSimilarPictures)
      responseSuccess(list)
    }
  }



}



