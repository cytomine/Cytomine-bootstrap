package be.cytomine.processing

import be.cytomine.Exception.CytomineException
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

class SoftwareParameterService extends ModelService{

   static transactional = true

    def cytomineService
    def transactionService
    def modelService
    def jobParameterService

    def currentDomain() {
        return SoftwareParameter
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        SoftwareParameter.list()
    }

    def read(def id) {
        def softParam = SoftwareParameter.read(id)
        //TODO: security?
        softParam
    }

    @PreAuthorize("#software.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Software software) {
        SoftwareParameter.findAllBySoftware(software)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkSoftwareAccess(#json['software']) or hasRole('ROLE_ADMIN')")
   def add(def json,SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkSoftwareWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) throws CytomineException {
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
        delete(retrieve(json))
    }

    def delete(SoftwareParameter sp, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${sp.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.name, domain.type, domain.software?.name]
    }


    def deleteDependentJobParameter(SoftwareParameter sp, Transaction transaction, Task task = null) {
        JobParameter.findAllBySoftwareParameter(sp).each {
            jobParameterService.delete(it,transaction,false)
        }
    }
}
