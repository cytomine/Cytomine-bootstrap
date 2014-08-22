package be.cytomine

import be.cytomine.api.RestController

class GrantRoleController extends RestController {

    def currentRoleServiceProxy
    def cytomineService

    def openAdminSession() {
        currentRoleServiceProxy.activeAdminSession(cytomineService.currentUser)
        responseSuccess(getCurrentRole())
    }

    def closeAdminSession() {
        currentRoleServiceProxy.closeAdminSession(cytomineService.currentUser)
        responseSuccess(getCurrentRole())
    }

    def infoAdminSession() {
        responseSuccess(getCurrentRole())
    }

    public def getCurrentRole() {
        def data = [:]
        def user = cytomineService.currentUser
        data['admin'] = currentRoleServiceProxy.isAdmin(user)
        data['user'] = !data['admin'] && currentRoleServiceProxy.isUser(user)
        data['guest'] = !data['admin'] && !data['user'] && currentRoleServiceProxy.isGuest(user)

        data['adminByNow'] = currentRoleServiceProxy.isAdminByNow(user)
        data['userByNow'] = currentRoleServiceProxy.isUserByNow(user)
        data['guestByNow'] = currentRoleServiceProxy.isGuestByNow(user)
        return data
    }
}
