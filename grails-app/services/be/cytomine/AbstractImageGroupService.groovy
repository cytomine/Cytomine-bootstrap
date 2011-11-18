package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.image.AbstractImageGroup
import be.cytomine.Exception.CytomineException
import be.cytomine.security.User
import be.cytomine.command.abstractimagegroup.AddAbstractImageGroupCommand
import be.cytomine.command.abstractimagegroup.DeleteAbstractImageGroupCommand

class AbstractImageGroupService {

    static transactional = true
    def cytomineService
    def commandService

    def get(AbstractImage abstractimage, Group group) {
       AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
    }

    def addAbstractImageGroup(def json) throws CytomineException{
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddAbstractImageGroupCommand(user: currentUser), json)
    }

    def deleteAbstractImageGroup(def json) throws CytomineException{
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new DeleteAbstractImageGroupCommand(user: currentUser), json)
    }
}
