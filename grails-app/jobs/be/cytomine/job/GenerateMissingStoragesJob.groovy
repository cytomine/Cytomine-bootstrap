package be.cytomine.job

import be.cytomine.image.server.Storage
import be.cytomine.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 17/05/13
 * Time: 11:30
 */
class GenerateMissingStoragesJob {

    def storageService

    /**
     *  This job must be concurrent = false
     *  If concurrent = true (default), and we add hundred users (time to create storage > 60000ms) => error
     */
    def concurrent = false

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
