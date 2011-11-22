package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.command.discipline.AddDisciplineCommand
import be.cytomine.command.discipline.EditDisciplineCommand
import be.cytomine.command.discipline.DeleteDisciplineCommand

class GroupService {

    static transactional = true
    def cytomineService
    def commandService

    def list() {
        return Group.list(sort:"name", order:"asc")
    }

     def list(def sortIndex, def sortOrder, def maxRows, def currentPage, def rowOffset, def name) {
        def groups = Group.createCriteria().list(max: maxRows, offset: rowOffset) {
            if (name!=null)
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
        commandService.processCommand(new AddDisciplineCommand(user: currentUser), json)
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new EditDisciplineCommand(user: currentUser), json)
    }

    def delete(def id) {
        User currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id : $id}")
        commandService.processCommand( new DeleteDisciplineCommand(user: currentUser,printMessage:true), json)
    }


}
