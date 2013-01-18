package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission

class OntologyService extends ModelService {

    static transactional = false
    def springSecurityService
    def transactionService
    def commandService
    def termService
    def cytomineService
    def domainService
    def securityService
    def aclUtilService

    boolean saveOnUndoRedoStack = true

    Ontology read(def id) {
        def ontology = Ontology.read(id)
        if(ontology) {
            SecurityCheck.checkReadAuthorization(ontology)
        }
        ontology
    }

    Ontology get(def id) {
        def ontology = Ontology.get(id)
        if(ontology) {
            SecurityCheck.checkReadAuthorization(ontology)
        }
        ontology
    }

    /**
     * List ontology with full tree structure (term, relation,...)
     * Security check is done inside method
     */
    def list() {
         def user = cytomineService.currentUser
         if(!user.admin) {
             def list = securityService.getOntologyList(user)
             return list
         } else {
             return Ontology.list()
         }
    }

    /**
     * List ontology with just id/name
     * Security check is done inside method
     */
    def listLight() {
        def ontologies = list()
        def data = []
        ontologies.each { ontology ->
            def ontologymap = [:]
            ontologymap.id = ontology.id
            ontologymap.name = ontology.name
            data << ontologymap
        }
        return data
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json,SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkOntologyWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkOntologyDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) throws CytomineException {
        //Start transaction
        Transaction transaction = transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
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
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(Ontology.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Ontology domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)

        //Add user creator as ontology admin
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)

        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Ontology.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Ontology domain, boolean printMessage) {
        if (domain && Project.findAllByOntology(domain).size() > 0) throw new ConstraintException("Ontology is still map with project")
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Ontology(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
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
        if (!ontology) {
            throw new ObjectNotFoundException("Ontology " + json.id + " not found")
        }
        return ontology
    }

}
