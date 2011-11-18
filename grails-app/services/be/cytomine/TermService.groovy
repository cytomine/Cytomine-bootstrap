package be.cytomine

import be.cytomine.security.User
import java.util.prefs.BackingStoreException
import java.sql.SQLException
import be.cytomine.ontology.Term
import be.cytomine.api.RestAnnotationTermController
import be.cytomine.api.RestSuggestedTermController
import be.cytomine.api.RestRelationTermController
import be.cytomine.command.term.DeleteTermCommand
import grails.converters.JSON
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.ConstraintException
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.image.ImageInstance
import be.cytomine.command.term.AddTermCommand
import be.cytomine.command.term.EditTermCommand
import be.cytomine.Exception.CytomineException

class TermService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService

    def list() {
        return Term.list()
    }

    def show(def id) {
        return Term.read(id)
    }

    def listByOntology(Ontology ontology) {
        return ontology?.leafTerms()
    }

    def listAllByProject(Project project) {
        return project?.ontology?.terms()
    }

    def listByImageInstance(ImageInstance image) {
        return image?.terms()
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



    def addTerm(def json) throws CytomineException{
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddTermCommand(user: currentUser), json)
    }

    def updateTerm(def json) throws CytomineException{
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new EditTermCommand(user: currentUser), json)
    }

    def deleteTerm(def id) throws CytomineException{
        User currentUser = cytomineService.getCurrentUser()
        return deleteTerm(id, currentUser)
    }

    def deleteTerm(def idTerm, User currentUser) throws CytomineException {
        return deleteTerm(idTerm, currentUser, true)
    }

    def deleteTerm(def idTerm, User currentUser, boolean printMessage) throws CytomineException {
        log.info "Delete term: " + idTerm
        Term term = Term.read(idTerm)
        if (term) {
            //Delete Annotation-Term before deleting Term
            new RestAnnotationTermController().deleteAnnotationTermFromAllUser(term, currentUser)

            //Delete Suggested-Term before deleting Term
            new RestSuggestedTermController().deleteSuggestedTermFromAllUser(term, currentUser)

            //Delete relation-Term before deleting Term
            new RestRelationTermController().deleteRelationTermFromTerm(term, currentUser)
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
            new RestRelationTermController().deleteRelationTermFromTerm(term, currentUser)
        }
        //Delete term
        def json = JSON.parse("{id : $idTerm}")
        def result = commandService.processCommand(new DeleteTermCommand(user: currentUser, printMessage: printMessage), json)
        return result
    }
}
