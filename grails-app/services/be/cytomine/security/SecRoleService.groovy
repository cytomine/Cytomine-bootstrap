package be.cytomine.security

import be.cytomine.SecurityACL

class SecRoleService {

    static transactional = true
    def cytomineService

    def read(def id) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        SecRole.read(id)
    }

    def findByAuthority(String authority) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        SecRole.findByAuthority(authority)
    }

    def list() {
        SecurityACL.checkGuest(cytomineService.currentUser)
        SecRole.list()
    }
}
