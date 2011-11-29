package be.cytomine.image

import be.cytomine.ModelService
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.Exception.CytomineException
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.Exception.ObjectNotFoundException

class AbstractImageGroupService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def responseService
    def domainService

    boolean saveOnUndoRedoStack = true

    def get(AbstractImage abstractimage, Group group) {
        AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
    }

    def add(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(JSONObject json, String commandType, boolean printMessage) {
        restore(AbstractImageGroup.createFromDataWithId(json),commandType,printMessage)
    }
    def restore(AbstractImageGroup domain, String commandType, boolean printMessage) {
        //Save new object
        domain = AbstractImageGroup.link(domain.abstractimage, domain.group)
        //Build response message
        return responseService.createResponseMessage(domain,[domain.id, domain.abstractimage.filename, domain.group.name],printMessage,commandType,domain.getCallBack())
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
        log.debug "json=" + json

         destroy(retrieve(json),commandType,printMessage)
    }
    def destroy(AbstractImageGroup domain, String commandType, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.abstractimage.filename, domain.group.name],printMessage,commandType,domain.getCallBack())
        //Delete object
        AbstractImageGroup.unlink(domain.abstractimage, domain.group)
        return response
    }

//    /**
//     * Edit domain which was previously edited
//     * @param json domain info
//     * @param commandType  command name (add/delete/...) which execute this method
//     * @param printMessage  print message or not
//     * @return response
//     */
//    def edit(JSONObject json, String commandType, boolean printMessage) {
//        //Rebuilt previous state of object that was previoulsy edited
//        edit(fillDomainWithData(new AbstractImageGroup(),json),commandType,printMessage)
//    }
//    def edit(AbstractImageGroup domain, String commandType, boolean printMessage) {
//        //Build response message
//        def response = responseService.createResponseMessage(domain,[domain.id, domain.abstractimage.filename, domain.group.name],printMessage,commandType,domain.getCallBack())
//        //Save update
//        domainService.saveDomain(domain)
//        return response
//    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AbstractImageGroup createFromJSON(def json) {
       return AbstractImageGroup.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        AbstractImage abstractimage = AbstractImage.get(json.abstractimage)
        Group group = Group.get(json.group)
        AbstractImageGroup domain = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
        if(!domain) throw new ObjectNotFoundException("AbstractImageGroup group=${json.group} image=${json.abstractimage} not found")
        return domain
    }
}
