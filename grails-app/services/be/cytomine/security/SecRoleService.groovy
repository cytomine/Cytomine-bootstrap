package be.cytomine.security

class SecRoleService {

    static transactional = true

    def list() {
        SecRole.list()
    }
}
