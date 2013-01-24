package be.cytomine.security

import org.springframework.security.access.prepost.PreAuthorize

class SecRoleService {

    static transactional = true

    @PreAuthorize("hasRole('ROLE_USER')")
    def read(def id) {
        SecRole.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list() {
        SecRole.list()
    }
}
