package be.cytomine

import be.cytomine.security.SecUser

class CytomineService {

    static transactional = false
    def springSecurityService

    SecUser getCurrentUser() {
        return SecUser.read(springSecurityService.principal.id)
    }

    boolean isUserAlgo() {
        return getCurrentUser().algo()
    }
}
