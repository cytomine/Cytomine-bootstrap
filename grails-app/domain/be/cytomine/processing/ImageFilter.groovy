package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.JSONUtils

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

import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

/**
 * An image filter applies image operations (Binary, Eosin,...)
 */
@RestApiObject(name = "image filter", description = "An image filter applies image operations (Binary, Eosin,...)")
class ImageFilter extends CytomineDomain {

    @RestApiObjectField(description = "The filter name",useForCreation = false)
    String name

    @RestApiObjectField(description = "The URL path of the filter on the processing server",useForCreation = false)
    String baseUrl

    @RestApiObjectField(description = "The URL of the processing server", allowedType = "string",useForCreation = false)
    ProcessingServer processingServer

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "id", description = "The domain id",allowedType = "long",useForCreation = false)
    ])
    static constraints = {
        name(blank: false, nullable: false)
        baseUrl(blank: false, nullable: false)
        processingServer (nullable: true)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static ImageFilter insertDataIntoDomain(def json,def domain=new ImageFilter()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        domain.baseUrl = JSONUtils.getJSONAttrStr(json, 'baseUrl')
        domain.processingServer = ProcessingServer.findByUrl(JSONUtils.getJSONAttrStr(json, 'processingServer'))
        if(!domain.processingServer) throw new WrongArgumentException("ProcessingServer doesn't exist")

        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = [:]
        returnArray['id'] = domain?.id
        returnArray['name'] = domain?.name
        returnArray['processingServer'] = domain?.processingServer?.url
        returnArray['baseUrl'] = domain?.baseUrl
        return returnArray
    }

    public CytomineDomain container() {
        return this;
    }

}
