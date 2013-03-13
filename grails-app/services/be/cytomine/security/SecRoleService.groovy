package be.cytomine.security

import be.cytomine.SecurityACL
import org.springframework.security.access.prepost.PreAuthorize

class SecRoleService {

    static transactional = true
    def cytomineService

    def read(def id) {
        SecurityACL.checkUser(cytomineService.currentUser)
        SecRole.read(id)
    }

    def list() {
        SecurityACL.checkUser(cytomineService.currentUser)
        SecRole.list()
    }
}
