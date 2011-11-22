package be.cytomine

import be.cytomine.security.SecRole

class SecRoleService {

    static transactional = true

    def list() {
       SecRole.list()
    }
}
