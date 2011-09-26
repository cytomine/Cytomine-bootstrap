package be.cytomine.api

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.image.ImageInstance
import be.cytomine.command.TransactionController

class RestAnnotationTermController extends RestController {

  def springSecurityService

  def listTermByAnnotation = {

    log.info "listByAnnotation with idAnnotation=" + params.idannotation
    if(params.idannotation=="undefined") responseNotFound("Annotation Term","Annotation", params.idannotation)
    else
    {
      Annotation annotation =  Annotation.read(params.idannotation)
      if(annotation!=null) responseSuccess(annotation.terms())
      else responseNotFound("Annotation Term","Annotation", params.idannotation)
    }

  }



  def listTermByAnnotationAndOntology = {
    log.info "listTermByAnnotationAndOntology with idAnnotation=" + params.idannotation + " and idOntology=" + params.idontology
    if(params.idannotation=="undefined") responseNotFound("Annotation Term","Annotation", params.idannotation)
    else
    {
      Annotation annotation =  Annotation.read(params.idannotation)
      Ontology ontology = Ontology.read(params.idontology)
      if(annotation!=null && ontology!=null)
      {
        def termsOntology = []
        def terms = annotation.terms()

        terms.each { term ->
          if(term.ontology.id==ontology.id)
          {
            termsOntology << term
          }
        }
        responseSuccess(termsOntology)
      }
      else responseNotFound("Annotation Term","Annotation", params.idannotation)
    }

  }

  def listAnnotationByTerm = {
    log.info "listByTerm with idTerm=" +  params.idterm
    Term term = Term.read(params.idterm)
    if(term!=null) responseSuccess(term.annotations())
    else responseNotFound("Annotation Term","Term", params.idterm)
  }

  def listAnnotationByProjectAndTerm = {
    log.info "listByTerm with idTerm=" +  params.idterm
    Term term = Term.read(params.idterm)
    Project project = Project.read(params.idproject)
    if(term==null) responseNotFound("Term", params.idterm)
    if(project==null) responseNotFound("Project", params.idproject)
    def annotationFromTermAndProject = []
    def annotationFromTerm = term.annotations()
    annotationFromTerm.each { annotation ->
      if(annotation.project()!=null && annotation.project().id == project.id)
        annotationFromTermAndProject << annotation
    }
    responseSuccess(annotationFromTermAndProject)
  }

  def listAnnotationByProjectAndImageInstance = {
      log.info "listByTerm with idTerm=" +  params.idterm + " and imageinstance=" +  params.idimageinstance
      Term term = Term.read(params.idterm)
      def annotations = []
      Annotation.findAllByImage(ImageInstance.read(params.idimageinstance)).each { annotation ->
        annotation.annotationTerm.each { annotationTerm->
            if (annotationTerm.getTerm() == term) annotations << annotation
        }
      }
      responseSuccess(annotations)
  }

  def show = {
    log.info "listByTerm with idTerm=" +  params.idterm + " idAnnotation=" + params.idannotation
    Annotation annotation = Annotation.read(params.idannotation)
    Term term = Term.read(params.idterm)
    if(annotation!=null && term!=null && AnnotationTerm.findByAnnotationAndTerm(annotation,term)!=null)
      responseSuccess(AnnotationTerm.findByAnnotationAndTerm(annotation,term))
    else  responseNotFound("Annotation Term","Term","Annotation", params.idterm,  params.idannotation)
  }


  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress + " request:" + request.JSON.toString()
    Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(addAnnotationTermCommand, currentUser)
    response(result)
  }


  def addWithDeletingOldTerm = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress + " request:" + request.JSON.toString()

    Annotation annotation = Annotation.get(params.idannotation)
    if(annotation) {
        log.info "Start transaction"
        TransactionController transaction = new TransactionController();
        transaction.start()


        def annotationTerm = AnnotationTerm.findAllByAnnotation(annotation)
        log.info "Delete old annotationTerm= " +annotationTerm.size()

        annotationTerm.each{ annotterm ->
            log.info "unlink annotterm:" +annotterm.id
            def postDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id]) as JSON
            Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData :postDataRT.toString() ,user: currentUser,printMessage:false)
            def result = processCommand(deleteAnnotationTermCommand, currentUser)
        }

        log.info "Add new annotationTerm with Annotation="  + params.idannotation + " Term=" + params.idterm
        def postData = ([annotation : params.idannotation,term :params.idterm]) as JSON
        Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : postData.toString(),user: currentUser)
        def result = processCommand(addAnnotationTermCommand, currentUser)
        transaction.stop()
        response(result)

    }
    else responseNotFound("Annotation",params.id)

  }







  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.idannotation=" + params.idannotation
    def postData = ([annotation : params.idannotation,term :params.idterm]) as JSON
    Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteAnnotationTermCommand, currentUser)
    response(result)
  }
}