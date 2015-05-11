package be.cytomine.security

import be.cytomine.CytomineDomain
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * User role (user, admin,...)
 */
@RestApiObject(name = "sec role", description="A user role on the full app (user, admin, guest,...)")
class SecRole extends CytomineDomain implements Serializable {

    @RestApiObjectField(description="The role name")
    String authority

    static mapping = {
        cache true
        id(generator: 'assigned', unique: true)
        sort "id"
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
        returnArray['id'] = domain?.id
        returnArray['authority'] = domain?.authority
        returnArray
    }
}
