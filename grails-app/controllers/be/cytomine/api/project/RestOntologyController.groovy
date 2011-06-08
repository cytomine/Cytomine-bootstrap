package be.cytomine.api.project

import be.cytomine.ontology.Term
import grails.converters.JSON
import grails.converters.XML
import be.cytomine.ontology.Ontology
import be.cytomine.security.User
import be.cytomine.command.UndoStackItem
import be.cytomine.command.Command
import be.cytomine.command.ontology.EditOntologyCommand
import be.cytomine.command.ontology.DeleteOntologyCommand
import be.cytomine.command.ontology.AddOntologyCommand

import be.cytomine.api.RestController
import be.cytomine.ontology.RelationTerm
import be.cytomine.command.TransactionController
import be.cytomine.command.term.DeleteTermCommand
import be.cytomine.command.relationterm.DeleteRelationTermCommand
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand


class RestOntologyController extends RestController {

  def springSecurityService
  def transactionService

  def list = {
    responseSuccess(Ontology.list())
  }

  def listByTerm = {
    log.info "listByTerm with term id:" + params.id
    Term term = Term.read(params.id);

    if(term != null) responseSuccess(term.ontology)
    else responseNotFound("Ontology","Term",params.id)
  }

  def show = {
    log.info "show with id:" + params.id
    Ontology ontology = Ontology.read(params.id)
    if(ontology!=null) responseSuccess(ontology)
    else responseNotFound("Ontology",params.id)
  }

  def showWithOnlyParentTerm = {
    log.info "show with id:" + params.id
    Ontology ontology = Ontology.read(params.id)
    log.info ontology
    def jsonOntology = ontology.encodeAsJSON()
    def jsonShow = JSON.parse(jsonOntology)

    jsonShow.children.each { child ->
      log.info child.children;
    }



    if(ontology!=null) responseSuccess(ontology)
    else responseNotFound("Ontology",params.id)
  }



  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command addOntologyCommand = new AddOntologyCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(addOntologyCommand, currentUser)
    response(result)
  }

  def update = {
    log.info "Update"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
    Command editOntologyCommand = new EditOntologyCommand(postData : request.JSON.toString(),user: currentUser)
    def result = processCommand(editOntologyCommand, currentUser)
    response(result)
  }

  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.id=" + params.id
    def postData = ([id : params.id]) as JSON


    log.info "Start transaction"
    TransactionController transaction = new TransactionController();
    transaction.start()

    Ontology ontology = Ontology.read(params.id)

    if(ontology) {
      def terms = ontology.terms()

      terms.each { term ->
        def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term,term)
        log.info "relationTerm= " +relationTerm.size()

        relationTerm.each{ relterm ->
          log.info "unlink relterm:" +relationTerm.id
          def postDataRT = ([relation :relterm.relation.id,term1: relterm.term1.id,term2: relterm.term2.id]) as JSON
          Command deleteRelationTermCommand = new DeleteRelationTermCommand(postData :postDataRT.toString() ,user: currentUser)
          def result = processCommand(deleteRelationTermCommand, currentUser)
        }

        def annotationTerm = AnnotationTerm.findAllByTerm(term)
        log.info "annotationTerm= " +relationTerm.size()

        annotationTerm.each{ annotterm ->
          log.info "unlink annotterm:" +annotterm.id
          def postDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id]) as JSON
          Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData :postDataRT.toString() ,user: currentUser)
          def result = processCommand(deleteAnnotationTermCommand, currentUser)
        }

        Term termDeleted =  term
        log.info "delete term " +termDeleted
        def postDataTerm = ([id : termDeleted.id]) as JSON
        Command deleteTermCommand = new DeleteTermCommand(postData :postDataTerm.toString() ,user: currentUser)
        def result = processCommand(deleteTermCommand, currentUser)

      }
    }
    log.info "delete ontology"
    Command deleteOntologyCommand = new DeleteOntologyCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteOntologyCommand, currentUser)

    log.info "End transaction"
    transaction.stop()
    response(result)
  }

}
