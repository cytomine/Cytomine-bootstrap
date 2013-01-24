package be.cytomine.security

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck

class UserGroupService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def modelService

    def list(User user) {
        UserGroup.findAllByUser(user)
    }

    def get(User user, Group group) {
        UserGroup.findByUserAndGroup(user, group)
    }

    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    def update(def json, SecurityCheck security) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(UserGroup.createFromData(json), printMessage)
    }

    def create(UserGroup domain, boolean printMessage) {
        //Save new object
        log.info "new usegroup " + domain
        domain.validate()
        domain.errors.each {
            log.info it
        }
        domain.setId(null)
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.user, domain.group], printMessage, "Add")
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
        destroy(domain, printMessage)
    }

    def destroy(UserGroup domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.user, domain.group], printMessage, "Delete", domain.getCallBack())
        //Delete object
        deleteDomain(domain)
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

    UserGroup link(User user, Group group) {
       log.info "link between " + user?.username + " " + group?.name
       def userGroup = UserGroup.findByUserAndGroup(user, group)
       if (!userGroup) {
           userGroup = new UserGroup(user:user, group:group)
            if (!userGroup.validate()) {
                log.info userGroup.retrieveErrors().toString()
                throw new WrongArgumentException(userGroup.retrieveErrors().toString())
            }
            if (!userGroup.save(flush: true)) {
                log.info userGroup.retrieveErrors().toString()
                throw new InvalidRequestException(userGroup.retrieveErrors().toString())
           }
       }
        log.info "userGroup=$userGroup"
       userGroup
   }

   void unlink(User user, Group group) {
       log.info "###################################"
       def userGroup = UserGroup.findByUserAndGroup(user, group)
       if (userGroup) {
           userGroup.delete(flush:true)

       } else {log.info "no link between " + user?.username + " " + group?.name}
   }


}
