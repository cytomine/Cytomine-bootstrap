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
import be.cytomine.image.AbstractImage
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

@RestApiObject(name = "storage abstract image", description="A link between a storage and some images")
class StorageAbstractImage extends CytomineDomain {

    @RestApiObjectField(description = "The storage id")
    Storage storage

    @RestApiObjectField(description = "The abstractimage id", apiFieldName = "abstractimage")
    AbstractImage abstractImage

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static StorageAbstractImage insertDataIntoDomain(def json, def domain = new StorageAbstractImage()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.storage = JSONUtils.getJSONAttrDomain(json, 'storage', new Storage(), true)
        domain.abstractImage = JSONUtils.getJSONAttrDomain(json, 'abstractimage', new AbstractImage(), true)
        domain.deleted = JSONUtils.getJSONAttrDate(json, "deleted")
        return domain
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['storage'] = domain?.storage?.id
        returnArray['abstractimage'] = domain?.abstractImage?.id
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return storage.container();
    }
}
