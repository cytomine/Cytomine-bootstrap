package be.cytomine.image

import be.cytomine.ModelService
import be.cytomine.command.abstractimagegroup.AddAbstractImageGroupCommand
import be.cytomine.command.abstractimagegroup.DeleteAbstractImageGroupCommand
import be.cytomine.security.Group
import be.cytomine.security.User

class AbstractImageGroupService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def responseService

    def get(AbstractImage abstractimage, Group group) {
        AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddAbstractImageGroupCommand(user: currentUser), json)
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new DeleteAbstractImageGroupCommand(user: currentUser), json)
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
    def restore(def json, String commandType, boolean printMessage) {
        //Rebuilt object that was previoulsy deleted
        def abstractimageGroup = AbstractImageGroup.createFromData(json)
        //Build response message
        def response = responseService.createResponseMessage(abstractimageGroup,[abstractimageGroup.id, abstractimageGroup.abstractimage.filename, abstractimageGroup.group.name],printMessage,commandType,abstractimageGroup.getCallBack())
        //Save new object
        AbstractImageGroup.link(abstractimageGroup.abstractimage, abstractimageGroup.group)
        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, String commandType, boolean printMessage) {
        //Destroy object that was previoulsy deleted
        def abstractimageGroup = AbstractImageGroup.createFromData(json)
        //Build response message
        def response = responseService.createResponseMessage(abstractimageGroup,[abstractimageGroup.id, abstractimageGroup.abstractimage.filename,abstractimageGroup.group.name],printMessage,commandType,abstractimageGroup.getCallBack())
        //Delete new object
        AbstractImageGroup.unlink(abstractimageGroup.abstractimage, abstractimageGroup.group)
        return response
    }
}
