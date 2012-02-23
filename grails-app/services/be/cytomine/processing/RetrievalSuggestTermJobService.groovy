package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.User
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.job.RetrievalSuggestTermJob

class RetrievalSuggestTermJobService extends JobService {

    static transactional = true
    //def cytomineService
    //def commandService
    //def domainService

    def list() {
        RetrievalSuggestTermJob.list()
    }

    def list(Project project) {
        RetrievalSuggestTermJob.findAllByProject(project)
    }

    def read(def id) {
        RetrievalSuggestTermJob.read(id)
    }

    def list(Software software) {
        RetrievalSuggestTermJob.findAllBySoftware(software)
    }

    def list(Software software, Project project) {
        RetrievalSuggestTermJob.findAllBySoftwareAndProject(software, project)
    }

//    def add(def json) {
//        User currentUser = cytomineService.getCurrentUser()
//        return executeCommand(new AddCommand(user: currentUser), json)
//    }
//
//    def update(def domain, Object json) {
//        User currentUser = cytomineService.getCurrentUser()
//        return executeCommand(new EditCommand(user: currentUser), json)
//    }

//    def delete(def domain, Object json) {
//        User currentUser = cytomineService.getCurrentUser()
//        //TODO: delete retrievalsuggesttermjob-parameters
//        //TODO: delete retrievalsuggesttermjob-data
//        return executeCommand(new DeleteCommand(user: currentUser), json)
//    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(RetrievalSuggestTermJob.createFromDataWithId(json), printMessage)
    }

//    def create(RetrievalSuggestTermJob domain, boolean printMessage) {
//        //Save new object
//        domainService.saveDomain(domain)
//        //Build response message
//        return responseService.createResponseMessage(domain, [domain.id, RetrievalSuggestTermJob], printMessage, "Add", domain.getCallBack())
//    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(RetrievalSuggestTermJob.get(json.id), printMessage)
    }

//    def destroy(RetrievalSuggestTermJob domain, boolean printMessage) {
//        //Build response message
//        def response = responseService.createResponseMessage(domain, [domain.id, RetrievalSuggestTermJob], printMessage, "Delete", domain.getCallBack())
//        //Delete object
//        domainService.deleteDomain(domain)
//        return response
//    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new RetrievalSuggestTermJob(), json), printMessage)
    }

//    def edit(RetrievalSuggestTermJob domain, boolean printMessage) {
//        //Build response message
//        def response = responseService.createResponseMessage(domain, [domain.id, RetrievalSuggestTermJob], printMessage, "Edit", domain.getCallBack())
//        //Save update
//        domainService.saveDomain(domain)
//        return response
//    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    RetrievalSuggestTermJob createFromJSON(def json) {
        return RetrievalSuggestTermJob.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        RetrievalSuggestTermJob retrievalsuggesttermjob = RetrievalSuggestTermJob.get(json.id)
        if (!retrievalsuggesttermjob) throw new ObjectNotFoundException("RetrievalSuggestTermJob " + json.id + " not found")
        return retrievalsuggesttermjob
    }
}
