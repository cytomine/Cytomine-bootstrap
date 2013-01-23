package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck
import org.springframework.security.access.prepost.PreAuthorize

class JobDataService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def domainService
    def userGroupService
    def springSecurityService

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
    def delete(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(JobData.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(JobData domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.key, domain.job?.id], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(JobData.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JobData domain, boolean printMessage) {
        //Build response message
        log.info "destroy JobData"
        log.info "createResponseMessage"
        def response = responseService.createResponseMessage(domain, [domain.id, domain.key, domain.job?.id], printMessage, "Delete", domain.getCallBack())
        //Delete object
        log.info "deleteDomain"
        domainService.deleteDomain(domain)
        log.info "response"
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
        edit(fillDomainWithData(new JobData(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JobData domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.key, domain.job?.id], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    JobData createFromJSON(def json) {
        return JobData.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        JobData jobdata = JobData.get(json.id)
        if (!jobdata) throw new ObjectNotFoundException("Jobdata " + json.id + " not found")
        return jobdata
    }
}
