package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON
import be.cytomine.utils.Task

class JobDataService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def modelService
    def userGroupService
    def springSecurityService
    def transactionService

    def currentDomain() {
        return JobData
    }

    def read(def id) {
        def jobData = JobData.read(id)
        if(jobData) {
            SecurityCheck.checkReadAuthorization(jobData.job.project)
        }
        jobData
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        JobData.list(sort: "id")
    }

    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Job job) {
        JobData.findAllByJob(job)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkJobAccess(#json['job']) or hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
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
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        delete(retrieve(json), transactionService.start())
    }

    def delete(JobData jobData, Transaction transaction, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${jobData.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.key, domain.job?.id]
    }

    def deleteDependentJobDataBinaryValue(JobData jobData, Transaction transaction, Task task = null) {
        println "jobData=$jobData"
        if(jobData.value) {
            jobData.value.delete()
        }
    }
}
