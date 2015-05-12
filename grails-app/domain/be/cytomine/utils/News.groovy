package be.cytomine.utils

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
import be.cytomine.security.User

/**
 * Cytomine new
 * Ex: "2013/08/10: Data from job can now be deleted"
 */
class News extends CytomineDomain {

    Date added
    String text
    User user

    static constraints = {
        text(type: 'text',nullable: false)
    }

    static mapping = {
        id generator: "assigned"
        text type: 'text'
        sort "id"
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['text'] = domain?.text
        returnArray['user'] = domain?.user?.id
        returnArray['added'] = domain?.added?.time?.toString()
        returnArray
    }
}
