package be.cytomine.api

import be.cytomine.ontology.Term
import grails.converters.JSON
import be.cytomine.ontology.Ontology
import be.cytomine.security.User
import be.cytomine.command.ontology.EditOntologyCommand
import be.cytomine.command.ontology.DeleteOntologyCommand
import be.cytomine.command.ontology.AddOntologyCommand
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

    def listLight = {
        def ontologies = Ontology.list()
        def data = []
        ontologies.each { ontology ->
            def ontologymap = [:]
            ontologymap.id  =  ontology.id
            ontologymap.name = ontology.name
            data << ontologymap
        }
        responseSuccess(data)
    }

    def listByTerm = {
        Term term = Term.read(params.id);
        if(term != null) responseSuccess(term.ontology)
        else responseNotFound("Ontology","Term",params.id)
    }

    def listByUserLight = {
        User user = getCurrentUser(springSecurityService.principal.id)

        if(user!=null) {
            def ontologies = user.ontologies()
            def data = []
            ontologies.each { ontology ->
                def ontologymap = [:]
                ontologymap.id  =  ontology.id
                ontologymap.name = ontology.name
                data << ontologymap
            }
            responseSuccess(data)
        }
        else responseNotFound("User",params.id)
    }
    def listByUser = {
        User user = getCurrentUser(springSecurityService.principal.id)
        if(user!=null) responseSuccess(user.ontologies())
        else responseNotFound("User",params.id)
    }

    def show = {
        Ontology ontology = Ontology.read(params.id)
        if(ontology!=null) responseSuccess(ontology)
        else responseNotFound("Ontology",params.id)
    }

    def showWithOnlyParentTerm = {
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
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddOntologyCommand(user: currentUser), request.JSON)
        response(result)
    }

    def update = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditOntologyCommand(user: currentUser), request.JSON)
        response(result)
    }

    def delete =  {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = ([id : params.id]) as JSON

        //Start transaction
        TransactionController transaction = new TransactionController();
        transaction.start()

        Ontology ontology = Ontology.read(params.id)

        if(ontology) {
            def terms = ontology.terms()
            //Delete term
            terms.each { term ->
                def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term,term)
                log.info "relationTerm= " +relationTerm.size()

                relationTerm.each{ relterm ->
                    log.info "unlink relterm:" +relationTerm.id
                    def jsonDataRT = ([relation :relterm.relation.id,term1: relterm.term1.id,term2: relterm.term2.id]) as JSON
                    def result = processCommand(new DeleteRelationTermCommand(user: currentUser,printMessage:false), jsonDataRT)
                }

                def annotationTerm = AnnotationTerm.findAllByTerm(term)
                log.info "annotationTerm= " +annotationTerm.size()

                annotationTerm.each{ annotterm ->
                    log.info "unlink annotterm:" +annotterm.id
                    def jsonDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id, user: annotterm.user.id]) as JSON
                    def result = processCommand(new DeleteAnnotationTermCommand(user: currentUser,printMessage:false), jsonDataRT)
                }

                Term termDeleted =  term
                log.info "delete term " +termDeleted
                def jsonDataTerm = ([id : termDeleted.id]) as JSON
                def result = processCommand( new DeleteTermCommand(user: currentUser,printMessage:false), jsonDataTerm)

            }
        }
        log.info "delete ontology"
        def result = processCommand(new DeleteOntologyCommand(user:currentUser,printMessage:true), json)

        log.info "End transaction"
        transaction.stop()
        response(result)
    }

}
