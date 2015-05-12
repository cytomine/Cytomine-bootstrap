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

import be.cytomine.image.NestedImageInstance
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageInstance to Cytomine with HTTP request during functional test
 */
class NestedImageInstanceAPI extends DomainAPI {

    static def listByImageInstance(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/nested.json"
        return doGET(URL, username, password)
    }

    static def show(Long id,Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested/${id}.json"
        return doGET(URL, username, password)
    }


    static def create(Long idImage,String jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested.json"
        def result = doPOST(URL,jsonImageInstance,username,password)
        result.data = NestedImageInstance.get(JSON.parse(result.data)?.nestedimageinstance?.id)
        return result
    }

    static def update(Long id, Long idImage, def jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested/${id}.json"
        return doPUT(URL,jsonImageInstance,username,password)
    }

    static def delete(Long id, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested/${id}.json"
        return doDELETE(URL,username,password)
    }

}
