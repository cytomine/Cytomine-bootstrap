package be.cytomine.processing

import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON

import static org.springframework.security.acls.domain.BasePermission.READ

class JobParameterService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    def currentDomain() {
        return JobParameter
    }

    def read(def id) {
        def jobParam = JobParameter.read(id)
        if(jobParam) {
            SecurityACL.check(jobParam.container(),READ)
        }
        jobParam
    }

    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        JobParameter.list()
    }

    def list(Job job) {
        SecurityACL.check(job.container(),READ)
        JobParameter.findAllByJob(job)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.job,Job,"container", READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(JobParameter jp, def jsonNewData) {
        SecurityACL.check(jp.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),jp, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(JobParameter domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),READ)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    /**
     * Add a job parameter for a job
     */
    def addJobParameter(def idJob, def idSoftwareParameter, def value,User currentUser,Transaction transaction) {
        def json = JSON.parse("{softwareParameter: $idSoftwareParameter, value: $value, job: $idJob}")
        return executeCommand(new AddCommand(user: currentUser,transaction:transaction),null,json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.value, domain.softwareParameter?.name]
    }
}
