package be.cytomine.api.project

import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.annotation.AddAnnotationCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.command.annotation.DeleteAnnotationCommand
import be.cytomine.command.annotation.EditAnnotationCommand
import be.cytomine.image.Image

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
        data.annotation = Annotation.findAllByImage(Image.findById(params.id))
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

  def listByUser = {
    log.info "List with id user:"+params.id
    def data = [:]

    if(User.findById(params.id)!=null) {
      data.annotation = Annotation.findAllByUser(User.findById(params.id))
    }
    else {
      log.error "User Id " + params.id+ " don't exist"
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("User not found with id : " + params.id)
        }
      }
    }

    withFormat {
      json { render data as JSON }
      xml { render jsonMap as XML}
    }
  }

  def listByImage = {
    log.info "List with id image:"+params.id
    def data = [:]

    if(Image.findById(params.id)!=null) {
      data.annotation = Annotation.findAllByImage(Image.findById(params.id))
    }
    else {
      log.error "Image Id " + params.id+ " don't exist"
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Image not found with id : " + params.id)
        }
      }
    }

    withFormat {
      json { render data as JSON }
      xml { render jsonMap as XML}
    }
  }

  def listByImageAndUser = {
    log.info "List with id image:"+params.idImage + " and id user:" + params.idUser
    def image = Image.get(params.idImage)
    def user = User.get(params.idUser)

    def data = [:]

    if(image && user) {
      data.annotation = Annotation.findAllByImageAndUser(image,user)
    }
    else if(!user){
      log.error "User Id " + params.idUser+ " don't exist"
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("User not found with id : " + params.idUser)
        }
      }
    } else if(!image){
      log.error "Image Id " + params.idImage+ " don't exist"
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Image not found with id : " + params.idImage)
        }
      }
    }

    withFormat {
      json { render data as JSON }
      xml { render jsonMap as XML}
    }
  }

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
    User currentUser = User.read(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    log.debug "Lastcommands="+UndoStackItem.findAllByUser(currentUser)

    Command addAnnotationCommand = new AddAnnotationCommand(postData : request.JSON.toString(), user: currentUser)
    def result = addAnnotationCommand.execute()
    if (result.status == 201) {
      addAnnotationCommand.save()
      new UndoStackItem(command : addAnnotationCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    log.debug "Lastcommands="+UndoStackItem.findAllByUser(currentUser)
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
     log.debug "Lastcommands="+UndoStackItem.findAllByUser(currentUser)
    Command deleteAnnotationCommand = new DeleteAnnotationCommand(postData : postData.toString(), user: currentUser)
    def result = deleteAnnotationCommand.execute()
    if (result.status == 200) {
      log.info "Save command on stack"
      deleteAnnotationCommand.save(flush:true)
      new UndoStackItem(command : deleteAnnotationCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
    }
    log.debug "Lastcommands="+UndoStackItem.findAllByUser(currentUser)
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }


  def update = {

    log.info "Update"
    User currentUser = User.read(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()

    def result

    if((String)params.id!=(String)request.JSON.annotation.id) {
      log.error "Annotation id from URL and from data are different:"+ params.id + " vs " +  request.JSON.annotation.id
      result = [data : [annotation : null , errors : ["Annotation id from URL and from data are different:"+ params.id + " vs " +  request.JSON.annotation.id ]], status : 400]
    }
    else
    {
      Command editAnnotationCommand = new EditAnnotationCommand(postData : request.JSON.toString(), user: currentUser)

      result = editAnnotationCommand.execute()
      if (result.status == 200) {
        log.info "Save command on stack"
        editAnnotationCommand.save()
        new UndoStackItem(command : editAnnotationCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save(flush:true)
      }
    }

    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }



}
