package be.cytomine

import be.cytomine.security.User
import be.cytomine.api.RestController
import grails.converters.JSON
import be.cytomine.command.TransactionController
import be.cytomine.ontology.Ontology
import be.cytomine.api.RestTermController
import be.cytomine.command.ontology.DeleteOntologyCommand
import be.cytomine.ontology.Term
import java.util.prefs.BackingStoreException
import org.hibernate.exception.ConstraintViolationException
import java.sql.SQLException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.command.ontology.AddOntologyCommand
import be.cytomine.command.ontology.EditOntologyCommand
import be.cytomine.Exception.CytomineException

class OntologyService extends CytomineService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def termService
    def cytomineService


    def list() {
        return Ontology.list()
    }

    def listLight() {
        def ontologies = Ontology.list()
        def data = []
        ontologies.each { ontology ->
            def ontologymap = [:]
            ontologymap.id = ontology.id
            ontologymap.name = ontology.name
            data << ontologymap
        }
        return data
    }

    def listByTerm(Term term) {
        return term?.ontology
    }

    def listByUserLight(User user) {
            def ontologies = user.ontologies()
            def data = []
            ontologies.each { ontology ->
                def ontologymap = [:]
                ontologymap.id = ontology.id
                ontologymap.name = ontology.name
                data << ontologymap
            }
            return data
    }
    def listByUser(User user){
        return user?.ontologies()
    }

    Ontology read(def id) {
        return Ontology.read(id)
    }

    Ontology get(def id) {
        return Ontology.get(id)
    }

    def addOntology(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new AddOntologyCommand(user: currentUser), json)
        return result
    }

    def updateOntology(def json) throws CytomineException{
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new EditOntologyCommand(user: currentUser), json)
        return result
    }


    def deleteOntology(def id)  throws CytomineException{
        //Start transaction
        transactionService.start()
        User currentUser = cytomineService.getCurrentUser()
        //Read ontology
        Ontology ontology = Ontology.read(id)

        //Delete each term from ontology (if possible)
        if(ontology) {
            log.info "Delete term from ontology"
            def terms = ontology.terms()
            terms.each { term ->
                termService.deleteTermRestricted(term.id, currentUser,false)
            }
        }
        //Delete ontology
        log.info "Delete ontology"
        def json = JSON.parse("{id : $id}")
        def result = commandService.processCommand(new DeleteOntologyCommand(user:currentUser,printMessage:true), json)

        //Stop transaction
        transactionService.stop()

        return result
    }



}
