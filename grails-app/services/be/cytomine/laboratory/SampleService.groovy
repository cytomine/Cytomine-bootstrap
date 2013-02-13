package be.cytomine.laboratory

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.AbstractImage
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize

import be.cytomine.command.Transaction

import grails.converters.JSON
import be.cytomine.Exception.ConstraintException
import be.cytomine.utils.Task

class SampleService extends ModelService {

    static transactional = true

    def cytomineService
    def abstractImageService
    def transactionService

    @Secured(['ROLE_ADMIN'])
    def list() {
        Sample.list()
    }

    //TODO:: secure ACL (from abstract image)
    def list(User user) {
        def abstractImageAvailable = abstractImageService.list(user)
        if(abstractImageAvailable.isEmpty()) {
            return []
        } else {
            AbstractImage.createCriteria().list {
                inList("id", abstractImageAvailable.collect{it.id})
                projections {
                    groupProperty('sample')
                }
            }
        }
    }

    //TODO:: secure ACL (if abstract image from sample is avaialbale for user)
    def read(def id) {
        Sample.read(id)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(Sample sample, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${sample.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(Sample.createFromData(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Sample domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Sample.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Sample domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        removeDomain(domain)
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
        edit(fillDomainWithData(new Sample(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(Sample domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Sample createFromJSON(def json) {
        return Sample.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Sample sample = Sample.read(json.id)
        if (!sample) {
            throw new ObjectNotFoundException("Sample " + json.id + " not found")
        }
        return sample
    }

    def deleteDependentAbstractImage(Sample sample, Transaction transaction, Task task = null) {
        AbstractImage.findAllBySample(sample).each {
            abstractImageService.delete(it,transaction,false)
        }
    }

    def deleteDependentSource(Sample sample, Transaction transaction, Task task = null) {
        //TODO: implement source cascade delete (first impl source command delete)
        if(Source.findAllBySample(sample)) {
            throw new ConstraintException("Sample has source. Cannot delete sample!")
        }
    }

}
