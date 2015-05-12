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
import be.cytomine.Exception.AlreadyExistException
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * A domain description (text, image,...)
 */
@RestApiObject(name = "description", description = "A domain description (text, image,...).")
class Description extends CytomineDomain implements Serializable {

    /**
     * text data
     */
    @RestApiObjectField(description = "Description text")
    String data

    /**
     * Domain class Name
     */
    @RestApiObjectField(description = "Domain class name")
    String domainClassName

    /**
     * Domain id
     */
    @RestApiObjectField(description = "Domain id")
    Long domainIdent

    static constraints = {
        data(type: 'text',nullable: false)
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Description.withNewSession {
            Description descriptionAlreadyExist = Description.findByDomainIdentAndDomainClassName(domainIdent,domainClassName)
            if(descriptionAlreadyExist && (descriptionAlreadyExist.id!=id))
                throw new AlreadyExistException("Domain $domainClassName with id $domainIdent already has description!")
        }
    }

    static mapping = {
        id generator: "assigned"
        data type: 'text'
        sort "id"
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */           
    static Description insertDataIntoDomain(def json,def domain = new Description()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.domainClassName = JSONUtils.getJSONAttrStr(json, 'domainClassName',true)
        domain.domainIdent = JSONUtils.getJSONAttrLong(json,'domainIdent',null)
        domain.data = JSONUtils.getJSONAttrStr(json, 'data',true)

        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')


        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['domainClassName'] = domain?.domainClassName
        returnArray['domainIdent'] = domain?.domainIdent
        returnArray['data'] = domain?.data //'<br/><img src="http://localhost:8080/api/attachedfile/8527848/download.png" align="left"><br/>' //description.data
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return getDomain()?.container()
    }

    public setDomain(CytomineDomain domain) {
        domainClassName = domain.class.name
        domainIdent = domain.id
    }


    public getDomain() {
        Class.forName(domainClassName, false, Thread.currentThread().contextClassLoader).read(domainIdent)
    }

}
