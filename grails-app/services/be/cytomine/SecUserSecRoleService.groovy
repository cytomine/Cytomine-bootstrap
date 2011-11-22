package be.cytomine

import be.cytomine.security.User
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.SecRole
import be.cytomine.command.secusersecrole.AddSecUserSecRoleCommand
import grails.converters.JSON
import be.cytomine.command.secusersecrole.DeleteSecUserSecRoleCommand

class SecUserSecRoleService {

    static transactional = true
    def cytomineService
    def commandService

    def list(User user) {
        SecUserSecRole.findAllBySecUser(user)
    }

    def get(User user,SecRole role) {
        SecUserSecRole.findBySecUserAndSecRole(user, role)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new AddSecUserSecRoleCommand(user: currentUser), json)
        return result
    }

    def delete(def user, def role) {
        User currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{user: $user, role: $role}")
        def result = commandService.processCommand(new DeleteSecUserSecRoleCommand(user: currentUser), json)
        return result
    }

    def list(def sortIndex, def sortOrder, def maxRows, def currentPage, def rowOffset)  {
        def secRoles = SecUserSecRole.createCriteria().list(max: maxRows, offset: rowOffset) {
            order(sortIndex, sortOrder).ignoreCase()
        }
        return secRoles
    }
}
