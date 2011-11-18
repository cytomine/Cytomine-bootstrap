package be.cytomine

import be.cytomine.security.User

class CytomineService {

    static transactional = false
      def springSecurityService

      User getCurrentUser() {
        return User.read(springSecurityService.principal.id)
      }
}
