package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class JobParameterService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    def list() {
        JobParameter.list()
    }

    def read(def id) {
        JobParameter.read(id)
    }

    def list(Job job) {
        JobParameter.findAllByJob(job)
    }

    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain, Object json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def domain, Object json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    //jobParameterService.addJobParameter(param.softwareParameter,param.value, currentUser, transaction);
    def addJobParameter(def idJob, def idSoftwareParameter, def value,User currentUser,Transaction transaction) {
        def json = JSON.parse("{softwareParameter: $idSoftwareParameter, value: $value, job: $idJob}")
        return executeCommand(new AddCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(JobParameter.createFromDataWithId(json), printMessage)
    }

    def create(JobParameter domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.value, domain.softwareParameter?.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(JobParameter.get(json.id), printMessage)
    }

    def destroy(JobParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.value, domain.softwareParameter?.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new JobParameter(), json), printMessage)
    }

    def edit(JobParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.value, domain.softwareParameter?.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
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
