package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.log4j.Logger

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
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static SecUserSecRole insertDataIntoDomain(def json,def domain = new SecUserSecRole()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.secUser = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        domain.secRole = JSONUtils.getJSONAttrDomain(json, "role", new SecRole(), true)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
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
