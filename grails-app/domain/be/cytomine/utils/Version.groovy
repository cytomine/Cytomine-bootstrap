package be.cytomine.utils

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
        log.info "Check is older $actual=actual and compared=$version (${actual.number<version})"
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
