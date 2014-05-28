package be.cytomine.utils

import be.cytomine.security.SecUser

class CytomineService implements Serializable {

    static transactional = false
    def springSecurityService

    SecUser getCurrentUser() {
        return SecUser.read(springSecurityService.principal.id)
    }

    boolean isUserAlgo() {
        return getCurrentUser().algo()
    }
}
