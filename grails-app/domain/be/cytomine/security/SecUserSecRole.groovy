package be.cytomine.security

import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder

class SecUserSecRole implements Serializable {

    SecUser secUser
    SecRole secRole

    static mapping = {
        id composite: ['secRole', 'secUser']
        version false
    }


    static SecUserSecRole get(long secUserId, long secRoleId) {
        find 'from SecUserSecRole where secUser.id=:secUserId and secRole.id=:secRoleId',
                [secUserId: secUserId, secRoleId: secRoleId]
    }

    static SecUserSecRole create(SecUser secUser, SecRole secRole, boolean flush = false) {
        new SecUserSecRole(secUser: secUser, secRole: secRole).save(flush: flush, insert: true)
    }

    static boolean remove(SecUser secUser, SecRole secRole, boolean flush = false) {
        SecUserSecRole instance = SecUserSecRole.findBySecUserAndSecRole(secUser, secRole)
        instance ? instance.delete(flush: flush) : false
    }

    static void removeAll(SecUser secUser) {
        executeUpdate 'DELETE FROM SecUserSecRole WHERE secUser=:secUser', [secUser: secUser]
    }

    static void removeAll(SecRole secRole) {
        executeUpdate 'DELETE FROM SecUserSecRole WHERE secRole=:secRole', [secRole: secRole]
    }

    static SecUserSecRole getFromData(SecUserSecRole secRole, jsonSecRole) {
        secRole.secUser = SecUser.read(jsonSecRole.user)
        secRole.secRole = SecRole.read(jsonSecRole.role)
        return secRole;
    }
      static SecUserSecRole createFromDataWithId(json)  {
        def domain = createFromData(json)
        try{domain.id = json.id}catch(Exception e){}
        return domain
    }
    static SecUserSecRole createFromData(data) {
        getFromData(new SecUserSecRole(), data)
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + SecUserSecRole.class
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
