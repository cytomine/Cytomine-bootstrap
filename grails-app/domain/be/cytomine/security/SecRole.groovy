package be.cytomine.security

import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObject

/**
 * User role (user, admin,...)
 */
@RestApiObject(name = "sec role", description="A user role on the full app (user, admin, guest,...)")
class SecRole {

    @RestApiObjectField(description="The role name")
    String authority

    static mapping = {
        cache true
    }

    static constraints = {
        authority blank: false, unique: true
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = [:]
        returnArray['authority'] = domain?.authority
        returnArray
    }
}
