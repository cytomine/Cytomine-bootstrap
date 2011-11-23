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
}
