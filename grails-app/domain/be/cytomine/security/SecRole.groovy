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
