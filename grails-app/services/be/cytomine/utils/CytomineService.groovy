package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.security.SecUser

class CytomineService implements Serializable {

    static transactional = false
    def springSecurityService

    SecUser getCurrentUser() {
        println "Spring security" + springSecurityService.principal.id
        return SecUser.read(springSecurityService.principal.id)
    }

    boolean isUserAlgo() {
        return getCurrentUser().algo()
    }

    public CytomineDomain getDomain(Long id,String className) {
        Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
    }

}
