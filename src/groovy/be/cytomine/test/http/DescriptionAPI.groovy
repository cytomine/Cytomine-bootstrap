package be.cytomine.test.http

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

import be.cytomine.test.Infos
import be.cytomine.utils.Description
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class DescriptionAPI extends DomainAPI {

    static def show(Long id, String className, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/description.json"
        return doGET(URL, username, password)
    }

    static def create(Long id, String className,String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        def result = doPOST(URL, json,username, password)
        Long descr = JSON.parse(result.data)?.description?.id
        return [data: Description.get(descr), code: result.code]
    }

    static def update(Long id, String className, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        return doPUT(URL,json,username,password)
    }

    static def delete(Long id, String className, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        return doDELETE(URL,username,password)
    }
}
