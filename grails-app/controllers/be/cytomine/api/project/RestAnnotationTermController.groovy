package be.cytomine.api.project

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