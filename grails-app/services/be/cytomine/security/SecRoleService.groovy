package be.cytomine.security

import be.cytomine.SecurityACL

class SecRoleService {

    static transactional = true
    def cytomineService

    def read(def id) {
        SecurityACL.checkGhest(cytomineService.currentUser)
        SecRole.read(id)
    }

    def list() {
        SecurityACL.checkGhest(cytomineService.currentUser)
        SecRole.list()
    }
}
