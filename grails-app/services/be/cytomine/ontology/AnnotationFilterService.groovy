package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class AnnotationFilterService extends ModelService {

    static transactional = true

    def cytomineService
    def domainService

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listByProject(Project project) {
        return AnnotationFilter.findAllByProject(project)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    AnnotationFilter read(def id) {
        def filter = AnnotationFilter.read(id)
        if(filter) {
            filter.project.checkReadPermission()
        }
        filter
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    AnnotationFilter get(def id) {
        def filter = AnnotationFilter.get(id)
        if(filter) {
            filter.project.checkReadPermission()
        }
        filter
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#domain.checkProjectAccess(#json['project'])")
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param json JSON with new data
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#model.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def update(def model,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param domain Domain to delete
     * @param json JSON that was passed in request parameter
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#model.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def delete(def model,def json) throws CytomineException {
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
        create(AnnotationFilter.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(AnnotationFilter domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(AnnotationFilter.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(AnnotationFilter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
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
        edit(fillDomainWithData(new AnnotationFilter(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(AnnotationFilter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }


    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AnnotationFilter createFromJSON(def json) {
        return AnnotationFilter.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON w
     * ith new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        AnnotationFilter annotationFilter = this.read(json.id)
        if (!annotationFilter) throw new ObjectNotFoundException("AnnotationFilter " + json.id + " not found")
        return annotationFilter
    }
}
