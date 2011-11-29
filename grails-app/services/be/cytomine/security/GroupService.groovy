package be.cytomine.security

import be.cytomine.ModelService
import be.cytomine.command.discipline.AddDisciplineCommand
import be.cytomine.command.discipline.DeleteDisciplineCommand
import be.cytomine.command.discipline.EditDisciplineCommand
import be.cytomine.image.AbstractImage
import be.cytomine.command.AddCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.Exception.ObjectNotFoundException

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
            order(sortIndex, sortOrder).ignoreCase()
        }
        return groups
    }

    def list(AbstractImage abstractimage) {
        return abstractimage.groups()
    }

    def read(def id) {
        return Group.read(id)
    }

    def get(def id) {
        return Group.get(id)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json)  {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def json)  {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(JSONObject json, String commandType, boolean printMessage) {
        restore(Group.createFromDataWithId(json),commandType,printMessage)
    }
    def restore(Group domain, String commandType, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType,domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, String commandType, boolean printMessage) {
        //Get object to delete
         destroy(Group.get(json.id),commandType,printMessage)
    }
    def destroy(Group domain, String commandType, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType,domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param commandType  command name (add/delete/...) which execute this method
     * @param printMessage  print message or not
     * @return response
     */
    def edit(JSONObject json, String commandType, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Group(),json),commandType,printMessage)
    }
    def edit(Group domain, String commandType, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.name],printMessage,commandType,domain.getCallBack())
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
        if(!group) throw new ObjectNotFoundException("Group " + json.id + " not found")
        return group
    }




}
