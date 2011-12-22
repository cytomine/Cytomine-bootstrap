package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.User
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.command.Transaction

class OntologyService extends ModelService {

    static transactional = false
    def springSecurityService
    def transactionService
    def commandService
    def termService
    def cytomineService
    def domainService

    boolean saveOnUndoRedoStack = true

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
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }



    def delete(def domain,def json) throws CytomineException {
        //Start transaction
        Transaction transaction = transactionService.start()
        User currentUser = cytomineService.getCurrentUser()
        //Read ontology
        Ontology ontology = Ontology.read(json.id)

        //Delete each term from ontology (if possible)
        if (ontology) {
            log.info "Delete term from ontology"
            def terms = ontology.terms()
            terms.each { term ->
                termService.deleteTermRestricted(term.id, currentUser, false,transaction)
            }
        }
        //Delete ontology
        log.info "Delete ontology"
        def result = executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)

        //Stop transaction
        transactionService.stop()

        return result
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Ontology.createFromDataWithId(json), printMessage)
    }

    def create(Ontology domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Ontology.get(json.id), printMessage)
    }

    def destroy(Ontology domain, boolean printMessage) {
        if (domain && Project.findAllByOntology(domain).size() > 0) throw new ConstraintException("Ontology is still map with project")
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Ontology(), json), printMessage)
    }

    def edit(Ontology domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Ontology createFromJSON(def json) {
        return Ontology.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Ontology ontology = Ontology.get(json.id)
        if (!ontology) throw new ObjectNotFoundException("Ontology " + json.id + " not found")
        return ontology
    }

}
