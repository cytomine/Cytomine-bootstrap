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
 * A group is a set of user
 * UserGroup is the link between a group and a user in database
 */
@RestApiObject(name = "user group", description="Link between a group and a user in database")
class UserGroup extends CytomineDomain {

    @RestApiObjectField(description = "The user id")
    User user

    @RestApiObjectField(description = "The group id")
    Group group

    static belongsTo = [user: User,group: Group]

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (user) builder.append(user.id)
        if (group) builder.append(group.id)
        builder.toHashCode()
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
	void checkAlreadyExist() {
        UserGroup.withNewSession {
            UserGroup userGroupAlreadyExist = UserGroup.findByUserAndGroup(user, group)
            if(userGroupAlreadyExist)  {
                throw new AlreadyExistException("UserGroup "+userGroupAlreadyExist?.user + ","+ userGroupAlreadyExist?.group + " already exist!")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static UserGroup insertDataIntoDomain(def json, def domain = new UserGroup()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.group = JSONUtils.getJSONAttrDomain(json, "group", new Group(), true)
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        return domain
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['user'] = domain?.user?.id
        returnArray['group'] = domain?.group?.id
        returnArray
    }

    def getCallBack() {
        return null
    }
}
