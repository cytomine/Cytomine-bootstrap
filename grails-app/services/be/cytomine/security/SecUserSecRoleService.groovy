package be.cytomine.security

import be.cytomine.ModelService
import be.cytomine.command.secusersecrole.AddSecUserSecRoleCommand
import be.cytomine.command.secusersecrole.DeleteSecUserSecRoleCommand

class SecUserSecRoleService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService

    def list(User user) {
        SecUserSecRole.findAllBySecUser(user)
    }

    def get(User user, SecRole role) {
        SecUserSecRole.findBySecUserAndSecRole(user, role)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new AddSecUserSecRoleCommand(user: currentUser), json)
        return result
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new DeleteSecUserSecRoleCommand(user: currentUser), json)
        return result
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def list(def sortIndex, def sortOrder, def maxRows, def currentPage, def rowOffset) {
        def secRoles = SecUserSecRole.createCriteria().list(max: maxRows, offset: rowOffset) {
            order(sortIndex, sortOrder).ignoreCase()
        }
        return secRoles
    }
}
