package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import be.cytomine.CytomineDomain

import grails.converters.JSON
import be.cytomine.utils.Task

class OntologyService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def termService

    def currentDomain() {
        return Ontology
    }

    Ontology read(def id) {
        def ontology = Ontology.read(id)
        if (ontology) {
            SecurityCheck.checkReadAuthorization(ontology)
        }
        ontology
    }

    Ontology get(def id) {
        def ontology = Ontology.get(id)
        if (ontology) {
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
        if (!user.admin) {
            def list = Ontology.executeQuery(
                    "select distinct ontology "+
                    "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser, Ontology as ontology "+
                    "where aclObjectId.objectId = ontology.id " +
                    "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                    "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
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
    def add(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
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
    def delete(def json, SecurityCheck security,Task task = null) throws CytomineException {
        return delete(retrieve(json),transactionService.start(),true,task)
    }

    def delete(Ontology ontology, Transaction transaction = null, boolean printMessage = true,Task task = null) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${ontology.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json,task)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def afterAdd(def domain, def response) {
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
    }

    def deleteDependentTerm(Ontology ontology, Transaction transaction, Task task = null) {
        Term.findAllByOntology(ontology).each {
            termService.delete(it,transaction, false)
        }
    }

    def deleteDependentProject(Ontology ontology, Transaction transaction, Task task = null) {
        if(Project.findByOntology(ontology)) {
            throw new ConstraintException("Ontology is linked with project. Cannot delete ontology!")
        }
    }
}
