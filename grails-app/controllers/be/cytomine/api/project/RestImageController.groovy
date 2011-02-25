package be.cytomine.api.project

import be.cytomine.project.Image
import grails.converters.*
import be.cytomine.project.Annotation
import be.cytomine.server.RetrievalServer
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.image.AddImageCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.project.Project
import be.cytomine.command.image.EditImageCommand
import be.cytomine.command.image.DeleteImageCommand

class RestImageController {

  def springSecurityService
  def transactionService

  /* REST API */
  def list = {
    log.info "list"
    def data = [:]
    data.image = Image.list()
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    log.info "show image " + params.id
    if(params.id && Image.exists(params.id)) {
      def data = [:]
      data.image = Image.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render ""
    }
  }

  def showByProject = {
    if(params.id && Project.exists(params.id)) {
      def scan = []
      Project.findAllById(params.id).each {
        it.projectSlide.each { ps ->
          ps.slide.scan.each { sc ->
            scan << sc
          }
        }
      }
      def resp = [ scan : scan]

      withFormat {
        json { render resp as JSON }
        xml { render resp as XML}
      }
    } else {
      response.status = 404
      render ""
    }
  }

  def index = {
    redirect(controller: "image")
  }

  def add = {

    log.info "Add"
    User currentUser = User.read(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    Command addImageCommand = new AddImageCommand(postData : request.JSON.toString())
    def result = addImageCommand.execute()
    if (result.status == 201) {
      addImageCommand.save(flush:true)
      new UndoStackItem(command : addImageCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
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

    if((String)params.id!=(String)request.JSON.image.id) {
      log.error "Image id from URL and from data are different:"+ params.id + " vs " +  request.JSON.image.id
      result = [data : [image : null , errors : ["Image id from URL and from data are different:"+ params.id + " vs " +  request.JSON.image.id ]], status : 400]
    }
    else
    {
      Command editImageCommand = new EditImageCommand(postData : request.JSON.toString())
      result = editImageCommand.execute()

      if (result.status == 200) {
        log.info "Save command on stack"
        //editImageCommand.transaction = transactionService.next(currentUser)
        editImageCommand.save()
        new UndoStackItem(command : editImageCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
      }
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def delete = {

    log.info "Delete"
    User currentUser = User.get(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id

    def postData = ([id : params.id]) as JSON

    Command deleteImageCommand = new DeleteImageCommand(postData : postData.toString())
    def result = deleteImageCommand.execute()
    if (result.status == 204) {
      log.info "Save command on stack"
      //deleteImageCommand.transaction = transactionService.next(currentUser)
      deleteImageCommand.save()
      new UndoStackItem(command : deleteImageCommand, user: currentUser,transactionInProgress:  currentUser.transactionInProgress).save(flush:true)

    }

        if (UndoStackItem.findAllByUser( currentUser).size() == 0) {
      log.error "Command stack is empty!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
      response.status = 404
      render ""
      return
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def thumb = {
    Image scan = Image.findById(params.id)
    print scan.getThumbURL()
    def out = new ByteArrayOutputStream()
    out << new URL(scan.getThumbURL()).openStream()
    response.contentLength = out.size();
    withFormat {
      jpg {
        if (request.method == 'HEAD') {
          render(text: "", contentType: "image/jpeg");
        }
        else {
          response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
        }
      }
    }
  }



  def metadata = {
    Image scan = Image.findById(params.id)
    def url = new URL(scan.getMetadataURL())
    withFormat {
      json {
        render(contentType: "application/json", text: "${url.text}")
      }
    }

  }

  def crop = {
    Annotation annotation = Annotation.findById(params.id)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle

    if (annotation == null || zoom < annotation.getImage().getZoomLevels().min || zoom > annotation.getImage().getZoomLevels().max) {
      response.status = 404
      render "404"
      return
    }

    def out = new ByteArrayOutputStream()
    out << new URL(annotation.getCropURL(zoom)).openStream()

    response.contentLength = out.size()

    withFormat {
      jpg {
        if (request.method == 'HEAD') {
          render(text: "", contentType: "image/jpeg");
        }
        else {
          response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
        }
      }
    }
  }

  def retrieval = {
    Annotation annotation = Annotation.findById(params.idannotation)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle
    int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)
    def retrievalServers = RetrievalServer.findAll()
    println annotation.getCropURL(1)
    def list = retrievalServers.get(0).search(annotation.getCropURL(zoom), maxSimilarPictures)

    withFormat {
      json { render list as JSON }
      xml { render list as XML}
    }
  }



}



