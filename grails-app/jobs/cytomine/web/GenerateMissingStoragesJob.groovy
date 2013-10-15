package cytomine.web

import be.cytomine.image.server.Storage

import be.cytomine.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 17/05/13
 * Time: 11:30
 */
class GenerateMissingStoragesJob {

    def storageService

    static triggers = {
        simple name: 'generateMissingStoragesJob', startDelay: 1000, repeatInterval: 60000
    }

    def execute() {
        SpringSecurityUtils.reauthenticate "lrollus", null
        for (user in User.findAll()) {
            if (!Storage.findByUser(user)) {
                println "generate missing storage fro $user"
                storageService.initUserStorage(user)
            }
        }
    }
}
