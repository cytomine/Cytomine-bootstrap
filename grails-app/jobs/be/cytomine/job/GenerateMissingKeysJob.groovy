package be.cytomine.job

import be.cytomine.security.SecUser

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 6/01/12
 * Time: 15:56
 */
class GenerateMissingKeysJob {

    static triggers = {
        simple name: 'generateMissingKeysJob', startDelay: 1000, repeatInterval: 60000*60
    }

    def execute() {
        Collection<SecUser> secUsers = SecUser.findAllByPrivateKeyIsNullOrPublicKeyIsNull()
        secUsers?.each { user ->
            user.generateKeys()
            user.save()
        }
    }
}
