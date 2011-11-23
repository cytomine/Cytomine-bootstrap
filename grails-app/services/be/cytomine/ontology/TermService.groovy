package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.ModelService
import be.cytomine.command.term.AddTermCommand
import be.cytomine.command.term.DeleteTermCommand
import be.cytomine.command.term.EditTermCommand
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON

class TermService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService
    def annotationTermService
    def suggestedTermService
    def relationTermService

    def list() {
        return Term.list()
    }

    Term read(def id) {
        return Term.read(id)
    }

    Term get(def id) {
        return Term.get(id)
    }

    def list(Ontology ontology) {
        return ontology?.leafTerms()
    }

    def list(Project project) {
        return project?.ontology?.terms()
    }

    def list(ImageInstance image) {
        return image?.terms()
    }


    def list(Annotation annotation, User user) {
        return AnnotationTerm.findAllByUserAndAnnotation(user, annotation).collect {it.term.id}
    }

    def statProject(Term term) {
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
        return convertHashToList(count)
    }

    List convertHashToList(HashMap<String, Integer> map) {
        def list = []
        map.each {
            list << ["key": it.key, "value": it.value]
        }
        list
    }

    def add(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddTermCommand(user: currentUser), json)
    }

    def update(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new EditTermCommand(user: currentUser), json)
    }

    def delete(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return deleteTerm(json.id, currentUser)
    }

    def deleteTerm(def idTerm, User currentUser) throws CytomineException {
        return deleteTerm(idTerm, currentUser, true)
    }

    def deleteTerm(def idTerm, User currentUser, boolean printMessage) throws CytomineException {
        log.info "Delete term: " + idTerm
        Term term = Term.read(idTerm)
        if (term) {
            //Delete Annotation-Term before deleting Term
            annotationTermService.deleteAnnotationTermFromAllUser(term, currentUser)

            //Delete Suggested-Term before deleting Term
            suggestedTermService.deleteSuggestedTermFromAllUser(term, currentUser)

            //Delete relation-Term before deleting Term
            relationTermService.deleteRelationTermFromTerm(term, currentUser)
        }
        //Delete term
        def json = JSON.parse("{id : $idTerm}")
        def result = commandService.processCommand(new DeleteTermCommand(user: currentUser, printMessage: printMessage), json)
        return result
    }

    def deleteTermRestricted(def idTerm, User currentUser, boolean printMessage) throws CytomineException {
        log.info "Delete term: " + idTerm
        Term term = Term.read(idTerm)
        if (term) {
            //Delete relation-Term before deleting Term
            relationTermService.deleteRelationTermFromTerm(term, currentUser)
        }
        //Delete term
        def json = JSON.parse("{id : $idTerm}")
        def result = commandService.processCommand(new DeleteTermCommand(user: currentUser, printMessage: printMessage), json)
        return result
    }
}
