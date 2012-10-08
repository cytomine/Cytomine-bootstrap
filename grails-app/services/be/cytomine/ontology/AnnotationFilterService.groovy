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

    @PreAuthorize("hasRole('ROLE_USER')")
    def listByProject(Project project) {
        return AnnotationFilter.findAllByProject(project)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    AnnotationFilter read(def id) {
        return AnnotationFilter.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    AnnotationFilter get(def id) {
        return AnnotationFilter.get(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def update(def model,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def delete(def model,def json) throws CytomineException {
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
        create(AnnotationFilter.createFromDataWithId(json), printMessage)
    }

    def create(AnnotationFilter domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(AnnotationFilter.get(json.id), printMessage)
    }

    def destroy(AnnotationFilter domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new AnnotationFilter(), json), printMessage)
    }

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
