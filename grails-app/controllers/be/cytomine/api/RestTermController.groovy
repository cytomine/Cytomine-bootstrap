package be.cytomine.api

import be.cytomine.ontology.Term

import grails.converters.JSON

import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.term.AddTermCommand

import be.cytomine.command.term.EditTermCommand
import be.cytomine.command.term.DeleteTermCommand

import be.cytomine.ontology.Ontology
import be.cytomine.image.AbstractImage
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.command.TransactionController
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.command.suggestedTerm.DeleteSuggestedTermCommand
import be.cytomine.ontology.RelationTerm
import be.cytomine.command.relationterm.DeleteRelationTermCommand

class RestTermController extends RestController {

    def springSecurityService
    def transactionService

    def list = {
        responseSuccess(Term.list())
    }

    def show = {
        Term term = Term.read(params.id)
        if (term) responseSuccess(term)
        else responseNotFound("Term", params.id)
    }

    def showFull = {
        Term term = Term.read(params.id)
        if (term) responseSuccess(term)
        else responseNotFound("Term", params.id)
    }

    def listByOntology = {
        Ontology ontology = Ontology.read(params.idontology)
        if (ontology) responseSuccess(ontology.leafTerms())
        else responseNotFound("Term", "Ontology", params.idontology)
    }

    def listAllByProject = {
        Project project = Project.read(params.idProject)
        if (project && project.ontology) responseSuccess(project.ontology.terms())
        else responseNotFound("Term", "Project", params.idProject)
    }

    def listByImageInstance = {
        ImageInstance image = ImageInstance.read(params.id)
        if (image) responseSuccess(image.terms())
        else responseNotFound("Term", "Image", params.id)
    }

    def statProject = {
        Term term = Term.read(params.id)

        if (term != null) {
            log.debug "term=" + term.name
            def projects = Project.findAllByOntology(term.ontology)

            log.debug "There are " + projects.size() + " projects for this ontology " + term.ontology.name
            def count = [:]
            def percentage = [:]

            //init list
            projects.each { project ->
                println "project=" + project.name
                count[project.name] = 0
                percentage[project.name] = 0
            }

            projects.each { project ->
                def annotations = project.annotations();
                annotations.each { annotation ->
                    if (annotation.terms().contains(term)) {
                        count[project.name] = count[project.name] + 1;
                    }
                }
            }

            //convert data map to list and merge term name and color
            responseSuccess(convertHashToList(count))

        }
        else responseNotFound("Project", params.id)
    }

    List convertHashToList(HashMap<String, Integer> map) {
        def list = []
        map.each {
            list << ["key": it.key, "value": it.value]
        }
        list
    }


    def add = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddTermCommand(user: currentUser), request.JSON)
        response(result)
    }


    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = ([id: params.id]) as JSON

        //Start transaction
        TransactionController transaction = new TransactionController();
        transaction.start()

        Term term = Term.read(params.id)

        if (term) {
            //delete annotation-term
            def annotationTerm = AnnotationTerm.findAllByTerm(term)
            log.info "annotationTerm= " + annotationTerm.size()

            annotationTerm.each { annotterm ->
                log.info "unlink annotterm:" + annotterm.id
                def jsonDataRT = ([term: annotterm.term.id, annotation: annotterm.annotation.id, user: annotterm.user.id]) as JSON
                def result = processCommand(new DeleteAnnotationTermCommand(user: currentUser, printMessage: false), jsonDataRT)
            }

            //delete suggest-term
            def suggestTerm = SuggestedTerm.findAllByTerm(term)
            log.info "suggestTerm= " + suggestTerm.size()

            suggestTerm.each { suggestterm ->
                log.info "unlink suggestterm:" + suggestterm.id
                def jsonDataRT = ([term: suggestterm.term.id, annotation: suggestterm.annotation.id, job: suggestterm.job.id]) as JSON
                def result = processCommand( new DeleteSuggestedTermCommand(user: currentUser, printMessage: false), jsonDataRT)
            }

            //delete relationTerm
            def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term, term)
            log.info "relationTerm= " + relationTerm.size()

            relationTerm.each { relationterm ->
                log.info "unlink relationterm:" + relationterm.id
                def jsonDataRT = ([relation: relationterm.relation.id, term1: relationterm.term1.id, term2: relationterm.term2.id]) as JSON
                def result = processCommand(new DeleteRelationTermCommand(user: currentUser), jsonDataRT)

            }
        }
        def result = processCommand(new DeleteTermCommand(user: currentUser), json)

        //Stop transaction
        transaction.stop()

        response(result)
    }

    def update = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditTermCommand(user: currentUser), request.JSON)
        response(result)
    }


}
