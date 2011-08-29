package be.cytomine.api

import be.cytomine.api.RestController
import be.cytomine.command.Command
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.image.AbstractImageGroup
import be.cytomine.command.abstractimagegroup.AddAbstractImageGroupCommand
import be.cytomine.command.abstractimagegroup.DeleteAbstractImageGroupCommand

class RestAbstractImageGroupController extends RestController {

  def springSecurityService

  def listGroupByAbstractImage = {

    log.info "listByAbstractImage with idAbstractImage=" + params.idabstractimage
    if(params.idabstractimage=="undefined") responseNotFound("AbstractImageGroup","AbstractImage", params.idabstractimage)
    else
    {
      AbstractImage abstractimage =  AbstractImage.read(params.idabstractimage)
      if(abstractimage!=null) responseSuccess(abstractimage.groups())
      else responseNotFound("AbstractImageGroup","AbstractImage", params.idabstractimage)
    }

  }

  def listAbstractImageByGroup = {
    log.info "listByGroup with idGroup=" +  params.idgroup
    Group group = Group.read(params.idgroup)
    if(group!=null) responseSuccess(group.abstractimages())
    else responseNotFound("AbstractImageGroup","Group", params.idgroup)
  }


  def show = {
    log.info "listByGroup with idGroup=" +  params.idgroup + " idAbstractImage=" + params.idabstractimage
    AbstractImage abstractimage = AbstractImage.read(params.idabstractimage)
    Group group = Group.read(params.idgroup)
    if(abstractimage!=null && group!=null && AbstractImageGroup.findByAbstractimageAndGroup(abstractimage,group)!=null)
      responseSuccess(AbstractImageGroup.findByAbstractimageAndGroup(abstractimage,group))
    else  responseNotFound("AbstractImageGroup","Group","AbstractImage", params.idgroup,  params.idabstractimage)
  }


  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress + " request:" + request.JSON.toString()
    Command addAbstractImageGroupCommand = new AddAbstractImageGroupCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(addAbstractImageGroupCommand, currentUser)
    response(result)
  }

  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.idabstractimage=" + params.idabstractimage
    def postData = ([abstractimage : params.idabstractimage,group :params.idgroup]) as JSON
    Command deleteAbstractImageGroupCommand = new DeleteAbstractImageGroupCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteAbstractImageGroupCommand, currentUser)
    response(result)
  }
}