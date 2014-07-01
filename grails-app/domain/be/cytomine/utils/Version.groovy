package be.cytomine.utils

import groovy.util.logging.Log

/**
 * Cytomine version history
 */
@Log
class Version {

    Long number
    Date deployed

    static mapping = {
        version false
        id generator: 'identity', column: 'nid'
    }

    static Version setCurrentVersion(Long version) {
        Version actual = getLastVersion()
        log.info "Last version was ${actual}. Actual version will be $version"
        if(actual && actual.number>=version) {
            log.info "version $actual don't need to be saved"
            return actual
        } else {
            log.info "New version detected"
            actual = new Version(number:version,deployed: new Date())
            actual.save(flush:true,failOnError: true)
            return actual
        }
    }

    static boolean isOlderVersion(Long version) {
        Version actual = getLastVersion()
        if(actual) {
            return actual.number<version
        } else return true
    }

    static Version getLastVersion() {
        def lastInList = Version.list(max:1,sort:"deployed",order:"desc")
        return lastInList.isEmpty()? null : lastInList.get(0)
    }

    String toString() {
        return "version ${number} (deployed ${deployed})"
    }
}
