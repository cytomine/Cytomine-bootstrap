package be.cytomine.security

class SecRoleService {

    static transactional = true

    def get(def id) {
        SecRole.get(id)
    }

    def read(def id) {
        SecRole.read(id)
    }

    def list() {
        SecRole.list()
    }

    def list(SecUser user) {
        SecUserSecRole.findAllBySecUser(user).collect { it.secRole } as Set
    }
}
