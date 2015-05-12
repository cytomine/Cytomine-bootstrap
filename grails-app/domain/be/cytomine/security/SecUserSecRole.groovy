package be.cytomine.security

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

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import org.apache.commons.lang.builder.HashCodeBuilder
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * User - role link
 * A user may have many role (user+admin for example)
 */
@RestApiObject(name = "sec user sec role", description="User - role link. A user may have many role (USER, ADMIN, GUEST)")
class SecUserSecRole extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The user id")
    SecUser secUser

    @RestApiObjectField(description = "The role id")
    SecRole secRole

    static mapping = {
        id generator: "assigned"
        sort "id"
        secRole lazy: false
    }


    static SecUserSecRole get(long secUserId, long secRoleId) {
        SecUserSecRole.findBySecRoleAndSecUser(SecRole.get(secRoleId),SecUser.get(secUserId))
    }

    static SecUserSecRole create(SecUser secUser, SecRole secRole, boolean flush = true) {
        if(!get(secUser.id,secRole.id)) {
            new SecUserSecRole(secUser: secUser, secRole: secRole).save(flush: flush, insert: true)
        }
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['user'] = domain?.secUser?.id
        returnArray['role'] = domain?.secRole?.id
        returnArray['authority'] = domain?.secRole?.authority
        returnArray
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

    void checkAlreadyExist() {
        SecUserSecRole.withNewSession {
            SecUserSecRole roleAlready = SecUserSecRole.findBySecUserAndSecRole(secUser,secRole)
            if(roleAlready && (roleAlready.id!=id))  throw new AlreadyExistException("Role ${secRole} already exist set for user ${secUser}!")
        }
    }
}
