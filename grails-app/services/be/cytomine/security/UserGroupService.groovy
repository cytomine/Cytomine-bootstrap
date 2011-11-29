package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject

class UserGroupService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def domainService

    def list(User user) {
        UserGroup.findAllByUser(user)
    }

    def get(User user, Group group) {
        UserGroup.findByUserAndGroup(user, group)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def json)  {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def restore(JSONObject json, boolean printMessage) {
        restore(UserGroup.createFromDataWithId(json),printMessage)
    }
    def restore(UserGroup domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain,[domain.id, domain.user,domain.group],printMessage,"Add",domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        User user = User.read(json.user)
        Group group = Group.read(json.group)
        UserGroup domain = UserGroup.findByUserAndGroup(user, group)
         destroy(domain,printMessage)
    }
    def destroy(UserGroup domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.user,domain.group],printMessage,"Delete",domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }


    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    UserGroup createFromJSON(def json) {
       return UserGroup.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        User user = User.read(json.user)
        Group group = Group.read(json.group)
        UserGroup domain = UserGroup.findByUserAndGroup(user, group)
        if (!domain) throw new ObjectNotFoundException("Usergroup with user $user and group $group not found")
        return domain
    }
}
