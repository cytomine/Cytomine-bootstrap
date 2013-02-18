package be.cytomine.processing

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
import grails.converters.JSON
import be.cytomine.utils.Task

class SoftwareService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService
    def aclUtilService
    def softwareParameterService
    def jobService
    def softwareProjectService

    def currentDomain() {
        Software
    }

    def read(def id) {
        //TODO: check authorization?
        Software.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    def list() {
        Software.list()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        SoftwareProject.findAllByProject(project).collect {it.software}
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) throws CytomineException {
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
    @PreAuthorize("#security.checkSoftwareWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) throws CytomineException {
        log.info "update software service"
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkSoftwareDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) throws CytomineException {
        //Start transaction
        return delete(retrieve(json),transactionService.start())
    }

    def delete(Software software, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${software.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }


    def afterAdd(def domain, def response) {
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentSoftwareParameter(Software software, Transaction transaction, Task task = null) {
        SoftwareParameter.findAllBySoftware(software).each {
            softwareParameterService.delete(it,transaction, false)
        }
    }

    def deleteDependentJob(Software software, Transaction transaction, Task task = null) {
        Job.findAllBySoftware(software).each {
            jobService.delete(it,transaction, false)
        }
    }

    def deleteDependentSoftwareProject(Software software, Transaction transaction, Task task = null) {
        SoftwareProject.findAllBySoftware(software).each {
            softwareProjectService.delete(it,transaction, false)
        }
    }
}
