package be.cytomine.security



class SecRoleService {

    static transactional = true
    def cytomineService
    def securityACLService

    def read(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        SecRole.read(id)
    }

    def findByAuthority(String authority) {
        securityACLService.checkGuest(cytomineService.currentUser)
        SecRole.findByAuthority(authority)
    }

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        SecRole.list()
    }
}
