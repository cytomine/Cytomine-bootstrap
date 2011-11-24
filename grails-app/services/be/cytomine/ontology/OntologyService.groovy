package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.ModelService
import be.cytomine.command.ontology.AddOntologyCommand
import be.cytomine.command.ontology.DeleteOntologyCommand
import be.cytomine.command.ontology.EditOntologyCommand
import be.cytomine.security.User

class OntologyService extends ModelService {

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

    def listByUser(User user) {
        return user?.ontologies()
    }

    Ontology read(def id) {
        return Ontology.read(id)
    }

    Ontology get(def id) {
        return Ontology.get(id)
    }

    def add(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new AddOntologyCommand(user: currentUser), json)
        return result
    }

    def update(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new EditOntologyCommand(user: currentUser), json)
        return result
    }


    def delete(def json) throws CytomineException {
        //Start transaction
        transactionService.start()
        User currentUser = cytomineService.getCurrentUser()
        //Read ontology
        Ontology ontology = Ontology.read(json.id)

        //Delete each term from ontology (if possible)
        if (ontology) {
            log.info "Delete term from ontology"
            def terms = ontology.terms()
            terms.each { term ->
                termService.deleteTermRestricted(term.id, currentUser, false)
            }
        }
        //Delete ontology
        log.info "Delete ontology"
        def result = commandService.processCommand(new DeleteOntologyCommand(user: currentUser, printMessage: true), json)

        //Stop transaction
        transactionService.stop()

        return result
    }



    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(def json, String commandType, boolean printMessage) {
        //Rebuilt object that was previoulsy deleted
        def domain = Ontology.createFromDataWithId(json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Save new object
        domain.save(flush: true)
        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, String commandType, boolean printMessage) {
         //Get object to delete
        def domain = Ontology.get(json.id)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Delete object
        domain.delete(flush: true)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param commandType  command name (add/delete/...) which execute this method
     * @param printMessage  print message or not
     * @return response
     */
    def edit(def json, String commandType, boolean printMessage) {
         //Rebuilt previous state of object that was previoulsy edited
        def domain = fillDomainWithData(new Ontology(),json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType)
        //Save update
        domain.save(flush: true)
        return response
    }
}
