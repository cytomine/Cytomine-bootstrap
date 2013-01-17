package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.AbstractImage
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck

class GroupService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    def list() {
        return Group.list(sort: "name", order: "asc")
    }

    def list(def sortIndex, def sortOrder, def maxRows, def currentPage, def rowOffset, def name) {
        def groups = Group.createCriteria().list(max: maxRows, offset: rowOffset) {
            if (name != null)
                ilike('name', "%$name%")
            order(sortIndex, sortOrder)
        }
        return groups
    }

    def list(AbstractImage abstractimage) {
        return abstractimage.abstractimagegroup.collect {
            it.group
        }
    }

    def list(User user) {
        UserGroup.findByUser(user).collect{it.group}
    }

    def read(def id) {
        return Group.read(id)
    }

    def get(def id) {
        return Group.get(id)
    }

    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def json, SecurityCheck security) {
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
        create(Group.createFromDataWithId(json), printMessage)
    }

    def create(Group domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Group.get(json.id), printMessage)
    }

    def destroy(Group domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new Group(), json), printMessage)
    }

    def edit(Group domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Group createFromJSON(def json) {
        return Group.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Group group = Group.get(json.id)
        if (!group) throw new ObjectNotFoundException("Group " + json.id + " not found")
        return group
    }

}
