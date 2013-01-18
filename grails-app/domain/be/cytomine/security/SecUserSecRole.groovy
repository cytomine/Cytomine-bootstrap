package be.cytomine.security

import be.cytomine.CytomineDomain
import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils

/**
 * User - role link
 * A user may have many role (user+admin for example)
 */
class SecUserSecRole extends CytomineDomain implements Serializable {

    SecUser secUser
    SecRole secRole

    static mapping = {
        id composite: ['secRole', 'secUser']
        version false
    }

    static SecUserSecRole get(long secUserId, long secRoleId) {
        SecUserSecRole.findBySecRoleAndSecUser(SecRole.get(secRoleId),SecUser.get(secUserId))
    }

    static SecUserSecRole create(SecUser secUser, SecRole secRole, boolean flush = true) {
        new SecUserSecRole(secUser: secUser, secRole: secRole).save(flush: flush, insert: true)
    }

    static boolean remove(SecUser secUser, SecRole secRole, boolean flush = false) {
        SecUserSecRole instance = SecUserSecRole.findBySecUserAndSecRole(secUser, secRole)
        instance ? instance.delete(flush: flush) : false
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static SecUserSecRole createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static SecUserSecRole createFromData(def json) {
        insertDataIntoDomain(new SecUserSecRole(), json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static SecUserSecRole insertDataIntoDomain(def domain, def json) {
        domain.secUser = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        domain.secRole = JSONUtils.getJSONAttrDomain(json, "role", new SecRole(), true)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + SecUserSecRole.class)
        JSON.registerObjectMarshaller(SecUserSecRole) {
            def returnArray = [:]
            returnArray['id'] = it.hashCode()
            returnArray['user'] = it.secUser.id
            returnArray['role'] = it.secRole.id
            return returnArray
        }
    }


    boolean equals(other) {
        if (!(other instanceof SecUserSecRole)) {
            return false
        }
        other.secUser?.id == secUser?.id && other.secRole?.id == secRole?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (secUser) builder.append(secUser.id)
        if (secRole) builder.append(secRole.id)
        builder.toHashCode()
    }
}
