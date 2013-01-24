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

class SoftwareParameterService extends ModelService{

   static transactional = true

    boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def modelService
    def responseService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        SoftwareParameter.list()
    }

    def read(def id) {
        def softParam = SoftwareParameter.read(id)
//        if(softParam) {
//            println "READ IMAGE PERMISSION="+ softParam.software.checkReadPermission()
//            SecurityCheck.checkReadAuthorization(softParam.software)
//        }
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
    def delete(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        def result = executeCommand(new DeleteCommand(user: currentUser), json)
        return result
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(SoftwareParameter.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(SoftwareParameter domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.name, domain.type, domain.software?.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(SoftwareParameter.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(SoftwareParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.name, domain.type, domain.software?.name], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new SoftwareParameter(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(SoftwareParameter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.name, domain.type, domain.software?.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    SoftwareParameter createFromJSON(def json) {
        return SoftwareParameter.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        SoftwareParameter parameter = SoftwareParameter.get(json.id)
        if (!parameter) throw new ObjectNotFoundException("SoftwareParameter " + json.id + " not found")
        return parameter
    }
}
