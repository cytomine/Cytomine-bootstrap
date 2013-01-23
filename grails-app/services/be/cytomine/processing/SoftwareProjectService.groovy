package be.cytomine.processing

import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
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
import be.cytomine.image.ImageInstance

class SoftwareProjectService extends ModelService{

   static transactional = true

    boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def domainService
    def responseService

    def read(def id) {
        def sp = SoftwareProject.get(id)
        if(sp) {
            SecurityCheck.checkReadAuthorization(sp.project)
        }
        sp
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        SoftwareProject.list()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        project.softwareProjects
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess(#json['project']) or hasRole('ROLE_ADMIN')")
   def add(def json,SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(SoftwareProject.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(SoftwareProject domain, boolean printMessage) {
        if(SoftwareProject.findBySoftwareAndProject(domain.software,domain.project)) throw new AlreadyExistException("Software  "+domain.software?.name + " already map with project "+domain.project?.name)
        //Save new object
        domain = SoftwareProject.link(domain.id,domain.software,domain.project)
        log.info "1. new domain="+ domain?.id
        //Build response message
        return responseService.createResponseMessage(domain, [domain.software?.name, domain.project?.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        log.info "JSON="+json
        destroy(SoftwareProject.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(SoftwareProject domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.software?.name, domain.project?.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        SoftwareProject.unlink(domain.software,domain.project)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    SoftwareProject createFromJSON(def json) {
        return SoftwareProject.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        SoftwareProject parameter = SoftwareProject.get(json.id)
        if (!parameter) throw new ObjectNotFoundException("SoftwareProject " + json.id + " not found")
        return parameter
    }
}
