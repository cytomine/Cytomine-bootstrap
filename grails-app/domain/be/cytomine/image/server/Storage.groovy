package be.cytomine.image.server

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
import be.cytomine.security.SecUser
import be.cytomine.utils.JSONUtils

/**
 * A storage is a remote file repository
 * It contains the network configuration, the credentials
 * and the remote path of the remote machine
 */
class Storage extends CytomineDomain {

    String name
    String basePath

    SecUser user //the creator, who got rights administration on the domain


    static constraints = {
        name(unique: false)
        basePath(nullable: false, blank: false)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def storage) {
        def returnArray = CytomineDomain.getDataFromDomain(storage)
        returnArray['name'] = storage?.name
        returnArray['basePath'] = storage?.basePath
        returnArray['user'] = storage?.user?.id
        returnArray
    }

    public boolean equals(Object o) {
        if (!o) {
            return false
        } else {
            try {
                return ((Storage) o).getId() == this.getId()
            } catch (Exception e) {
                return false
            }
        }

    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Storage insertDataIntoDomain(def json,def domain = new Storage()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name',true)
        domain.basePath = JSONUtils.getJSONAttrStr(json, 'basePath',true)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), false)
        return domain
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }
}
