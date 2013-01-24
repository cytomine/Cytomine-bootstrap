package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.utils.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck
import org.springframework.security.access.prepost.PreAuthorize

class JobParameterService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService


    def read(def id) {
        def jobParam = JobParameter.read(id)
        if(jobParam) {
            SecurityCheck.checkReadAuthorization(jobParam.job.project)
        }
        jobParam
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        JobParameter.list()
    }

    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Job job) {
        JobParameter.findAllByJob(job)
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
     * Add a job parameter for a job
     */
    def addJobParameter(def idJob, def idSoftwareParameter, def value,User currentUser,Transaction transaction) {
        def json = JSON.parse("{softwareParameter: $idSoftwareParameter, value: $value, job: $idJob}")
        return executeCommand(new AddCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(JobParameter.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(JobParameter domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.value, domain.softwareParameter?.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(JobParameter.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JobParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.value, domain.softwareParameter?.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        deleteDomain(domain)
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
        edit(fillDomainWithData(new JobParameter(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JobParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.value, domain.softwareParameter?.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    JobParameter createFromJSON(def json) {
        return JobParameter.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        JobParameter jobParameter = JobParameter.get(json.id)
        if (!jobParameter) throw new ObjectNotFoundException("JobParameter " + json.id + " not found")
        return jobParameter
    }
}
