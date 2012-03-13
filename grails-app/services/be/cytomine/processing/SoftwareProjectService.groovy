package be.cytomine.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.project.Project

class SoftwareProjectService extends ModelService{

   static transactional = true

    boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def domainService
    def responseService

    def list() {
        SoftwareProject.list()
    }

    def read(def id) {
        SoftwareProject.read(id)
    }

    def list(Project project) {
        project.softwareProjects
    }

   def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def domain,def json) throws CytomineException {
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

    def create(SoftwareProject domain, boolean printMessage) {
        if(SoftwareProject.findBySoftwareAndProject(domain.software,domain.project)) throw new WrongArgumentException("Software  "+domain.software?.name + " already map with project "+domain.project?.name)
        //Save new object
        domain = SoftwareProject.link(domain.id,domain.software,domain.project)
        log.info "1. new domain="+ domain?.id
        //Build response message
        return responseService.createResponseMessage(domain, [domain.software?.name, domain.project?.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        log.info "JSON="+json
        destroy(SoftwareProject.get(json.id), printMessage)
    }

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
