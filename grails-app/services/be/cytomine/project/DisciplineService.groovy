package be.cytomine.project

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
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

    def list() {
        SecurityACL.checkUser(cytomineService.currentUser)
        Discipline.list()
    }

    def read(def id) {
        SecurityACL.checkUser(cytomineService.currentUser)
        Discipline.read(id)
    }

    def get(def id) {
        SecurityACL.checkUser(cytomineService.currentUser)
        Discipline.get(id)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Discipline discipline, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        return executeCommand(new EditCommand(user: currentUser),discipline, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Discipline domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
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
