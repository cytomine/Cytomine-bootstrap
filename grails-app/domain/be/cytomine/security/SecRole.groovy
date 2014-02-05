package be.cytomine.security

import be.cytomine.CytomineDomain
import jsondoc.annotation.ApiObjectFieldLight
import org.jsondoc.core.annotation.ApiObject

/**
 * User role (user, admin,...)
 */
@ApiObject(name = "sec role", description="A user role on the full app (user, admin, guest,...)")
class SecRole {

    @ApiObjectFieldLight(description="The role name")
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
