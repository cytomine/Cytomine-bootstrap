package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

class OntologyService extends ModelService {

    static transactional = true
    boolean saveOnUndoRedoStack = true

    def springSecurityService
    def transactionService
    def termService

    def currentDomain() {
        return Ontology
    }

    Ontology read(def id) {
        def ontology = Ontology.read(id)
        if (ontology) {
            SecurityACL.check(ontology,READ)
        }
        ontology
    }

    Ontology get(def id) {
        def ontology = Ontology.get(id)
        if (ontology) {
            SecurityACL.check(ontology,READ)
        }
        ontology
    }

    /**
     * List ontology with full tree structure (term, relation,...)
     * Security check is done inside method
     */
    def list() {
        def user = cytomineService.currentUser
        return SecurityACL.getOntologyList(user)
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
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkUser(currentUser)
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Ontology domain, def jsonNewData) throws CytomineException {
        SecurityACL.check(domain,WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),domain,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Ontology domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain,DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def afterAdd(def domain, def response) {
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
    }

    def deleteDependentTerm(Ontology ontology, Transaction transaction, Task task = null) {
        Term.findAllByOntology(ontology).each {
            termService.delete(it,transaction, null,false)
        }
    }

    def deleteDependentProject(Ontology ontology, Transaction transaction, Task task = null) {
        if(Project.findByOntology(ontology)) {
            throw new ConstraintException("Ontology is linked with project. Cannot delete ontology!")
        }
    }
}
