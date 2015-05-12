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
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * A group is a set of user
 */
@RestApiObject(name = "group", description="A group is a set of users. A user may be in many groups")
class Group extends CytomineDomain {

    @RestApiObjectField(description="The group name")
    String name

    @RestApiObjectField(description = "The id for external connection (LDAP, etc.)")
    String gid


    static mapping = {
        table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
        sort "id"
    }

    static constraints = {
        name(blank: false, unique: true)
        gid(nullable: true)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Group insertDataIntoDomain(def json,def domain=new Group()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json,'name',true)
        domain.gid = JSONUtils.getJSONAttrStr(json,'gid',false)
        return domain;
    }

    String toString() {
        name
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['gid'] = domain?.gid
        returnArray
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Group.withNewSession {
            Group groupAlreadyExist = Group.findByName(name)
            if(groupAlreadyExist && (groupAlreadyExist.id!=id))  throw new AlreadyExistException("Group $name already exist!")
        }
    }

}
