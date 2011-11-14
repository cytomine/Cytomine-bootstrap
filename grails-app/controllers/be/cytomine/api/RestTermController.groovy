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
        log.info "Show:" + params.id
        Term term = Term.read(params.id)
        if (term) responseSuccess(term)
        else responseNotFound("Term", params.id)
    }

    def showFull = {
        log.info "Show:" + params.id
        Term term = Term.read(params.id)
        if (term) responseSuccess(term)
        else responseNotFound("Term", params.id)
    }

    def listByOntology = {
        log.info "listByOntology " + params.idontology
        Ontology ontology = Ontology.read(params.idontology)
        if (ontology) responseSuccess(ontology.leafTerms())
        else responseNotFound("Term", "Ontology", params.idontology)
    }

    def listAllByProject = {
        log.info "listAllByProject " + params.idProject
        Project project = Project.read(params.idProject)
        if (project && project.ontology) responseSuccess(project.ontology.terms())
        else responseNotFound("Term", "Project", params.idProject)
    }

    def listByImageInstance = {
        log.info "listByImage " + params.id
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
            println "Item: $it"
            list << ["key": it.key, "value": it.value]
        }
        list
    }



    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command addTermCommand = new AddTermCommand(postData: request.JSON.toString(), user: currentUser)
        def result = processCommand(addTermCommand, currentUser)
        response(result)
    }


    def delete = {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " params.id=" + params.id
        def postData = ([id: params.id]) as JSON

        log.info "Start transaction"
        TransactionController transaction = new TransactionController();
        transaction.start()

        Term term = Term.read(params.id)

        if (term) {
            //delete annotation
            def annotationTerm = AnnotationTerm.findAllByTerm(term)
            log.info "annotationTerm= " + annotationTerm.size()

            annotationTerm.each { annotterm ->
                log.info "unlink annotterm:" + annotterm.id
                def postDataRT = ([term: annotterm.term.id, annotation: annotterm.annotation.id]) as JSON
                Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData: postDataRT.toString(), user: currentUser, printMessage: false)
                def result = processCommand(deleteAnnotationTermCommand, currentUser)
            }

            //delete suggestTerm
            def suggestTerm = SuggestedTerm.findAllByTerm(term)
            log.info "suggestTerm= " + suggestTerm.size()

            suggestTerm.each { suggestterm ->
                log.info "unlink suggestterm:" + suggestterm.id
                def postDataRT = ([term: suggestterm.term.id, annotation: suggestterm.annotation.id, job: suggestterm.job.id]) as JSON
                Command deleteSuggestedTermCommand = new DeleteSuggestedTermCommand(postData: postDataRT.toString(), user: currentUser, printMessage: false)
                def result = processCommand(deleteSuggestedTermCommand, currentUser)
            }

            //delete relationTerm
            def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term, term)
            log.info "relationTerm= " + relationTerm.size()

            relationTerm.each { relationterm ->
                log.info "unlink relationterm:" + relationterm.id
                def postDataRT = ([relation: relationterm.relation.id, term1: relationterm.term1.id, term2: relationterm.term2.id]) as JSON
                Command deleteRelationTermCommand = new DeleteRelationTermCommand(postData: postDataRT.toString(), user: currentUser)
                def result = processCommand(deleteRelationTermCommand, currentUser)

            }
        }
        Command deleteTermCommand = new DeleteTermCommand(postData: postData.toString(), user: currentUser)
        def result = processCommand(deleteTermCommand, currentUser)
        response(result)
    }





























    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editTermCommand = new EditTermCommand(postData: request.JSON.toString(), user: currentUser)
        def result = processCommand(editTermCommand, currentUser)
        response(result)
    }


}
