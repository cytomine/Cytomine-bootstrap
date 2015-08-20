package be.cytomine.processing

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

/**
 * TODOSTEVBEN: doc
 */
class ProcessingServer extends CytomineDomain {

    String url

    static constraints = {
        url nullable: false, unique: true
    }

    void checkAlreadyExist() {
        ProcessingServer.withNewSession {
            if(url) {
                ProcessingServer psSameName = ProcessingServer.findByUrl(url)
                if(psSameName&& (psSameName.id!=id))  {
                    throw new AlreadyExistException("ProcessingServer "+psSameName.url + " already exist!")
                }
            }

        }
    }

    static ProcessingServer insertDataIntoDomain(def json,def domain=new ProcessingServer()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.url = JSONUtils.getJSONAttrStr(json, 'url')
        return domain;
    }

    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['url'] = domain?.url
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }

}
