package be.cytomine.api.project

import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.annotation.AddAnnotationCommand
import be.cytomine.command.annotation.DeleteAnnotationCommand
import be.cytomine.command.annotation.EditAnnotationCommand
import be.cytomine.image.Image
import be.cytomine.api.RestController
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.project.Project

class RestAnnotationController extends RestController {

  def springSecurityService


  def list = {
    def data = Annotation.list()
    responseSuccess(data)
  }

  def listByImage = {
    log.info "List with id image:"+params.id
    Image image = Image.read(params.id)

    if(image!=null) responseSuccess(Annotation.findAllByImage(image))
    else responseNotFound("Image",params.id)
  }

  def listByUser = {
    log.info "List with id user:"+params.id
    User user = User.read(params.id)

    if(user!=null) responseSuccess(Annotation.findAllByUser(user))
    else responseNotFound("User",params.id)
  }

  def listByProject = {
    log.info "List with id user:"+params.id
    Project project = Project.read(params.id)
    if(project)
    {
    def images = project.images()
    def annotations = []
    images.each() {
        annotations.addAll(Annotation.findAllByImage(it))
    }
      responseSuccess(annotations)
    }
    else responseNotFound("Project",params.id)
  }

  def listByImageAndUser = {
    log.info "List with id image:"+params.idImage + " and id user:" + params.idUser
    def image = Image.read(params.idImage)
    def user = User.read(params.idUser)

    if(image && user) responseSuccess(Annotation.findAllByImageAndUser(image,user))
    else if(!user) responseNotFound("User",params.idUser)
    else if(!image) responseNotFound("Image",params.idImage)
  }

  def show = {
    log.info "Show with id:" + params.id
    Annotation annotation = Annotation.read(params.id)

    if(annotation!=null) responseSuccess(annotation)
    else responseNotFound("Annotation",params.id)
  }

  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " transaction:" +  currentUser.transactionInProgress  + " request:" + request.JSON.toString()
    Command addAnnotationCommand = new AddAnnotationCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(addAnnotationCommand, currentUser)
    response(result)
  }


  def delete = {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress  + " params.id=" + params.id
    //TODO: delete annotation-term if annotation is deleted
    //This code seems to be ok BUT it's not done with command (no undo/redo) !
    /*Annotation.withTransaction {
      Annotation annotation = Annotation.read(params.id)
      List<AnnotationTerm> annotationTerms
      if(annotation)
        annotationTerms = AnnotationTerm.findAllByAnnotation(annotation)
      log.debug "annotationTerms =" + annotationTerms
      if(annotationTerms) {
        log.info "the annotation has " + annotationTerms.size() + " term mapped"
        annotationTerms*.delete()
      } */
      def postData = ([id : params.id]) as JSON
      Command deleteAnnotationCommand = new DeleteAnnotationCommand(postData : postData.toString(), user: currentUser)
      def result = processCommand(deleteAnnotationCommand, currentUser)
      response(result)

    //}

  }


  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editAnnotationCommand = new EditAnnotationCommand(postData : request.JSON.toString(), user: currentUser)
    def result = processCommand(editAnnotationCommand, currentUser)
    response(result)
  }

}
