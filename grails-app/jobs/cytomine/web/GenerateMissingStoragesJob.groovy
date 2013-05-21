package cytomine.web

import be.cytomine.image.server.Storage

import be.cytomine.security.User

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 17/05/13
 * Time: 11:30
 */
class GenerateMissingStoragesJob {

    def storageService

    static triggers = {
        simple name: 'generateMissingStoragesJob', startDelay: 1000, repeatInterval: 60000*60
    }

    def execute() {
        for (user in User.findAll()) {
            if (!Storage.findByUser(user)) {
                storageService.initUserStorage(user)
            }
        }
    }
}
