package be.cytomine.project

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.ontology.Ontology
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON
import be.cytomine.utils.Task

class DisciplineService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService
    def transactionService

    def currentDomain() {
        Discipline
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list() {
        Discipline.list()
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def read(def id) {
        Discipline.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def get(def id) {
        Discipline.get(id)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(Discipline discipline, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${discipline.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentProject(Discipline discipline, Transaction transaction, Task task = null) {
        if(Project.findByDiscipline(discipline)) {
            throw new ConstraintException("Discipline is linked with project. Cannot delete discipline!")
        }
    }

}
