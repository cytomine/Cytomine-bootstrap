package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

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
    def delete(def json, SecurityCheck security, Task task = null) {
        delete(retrieve(json))
    }

    def delete(JobParameter jp, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${jp.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Add a job parameter for a job
     */
    def addJobParameter(def idJob, def idSoftwareParameter, def value,User currentUser,Transaction transaction) {
        def json = JSON.parse("{softwareParameter: $idSoftwareParameter, value: $value, job: $idJob}")
        return executeCommand(new AddCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.value, domain.softwareParameter?.name]
    }
}
