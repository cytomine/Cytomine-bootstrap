package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON
import be.cytomine.utils.Task
import static org.springframework.security.acls.domain.BasePermission.*

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
            SecurityACL.check(jobData.projectDomain(),READ)
        }
        jobData
    }

    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        JobData.list(sort: "id")
    }

    def list(Job job) {
        SecurityACL.check(job.projectDomain(),READ)
        JobData.findAllByJob(job)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.job, Job,"projectDomain",READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(JobData jd, def jsonNewData) {
        SecurityACL.check(jd.projectDomain(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),jd,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(JobData domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.projectDomain(),READ)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
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
