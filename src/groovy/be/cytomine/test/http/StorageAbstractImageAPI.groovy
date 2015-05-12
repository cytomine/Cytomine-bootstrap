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

import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/02/13
 * Time: 11:03
 */
class StorageAbstractImageAPI extends DomainAPI {

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage_abstract_image.json"
        def result =  doPOST(URL,json,username,password)
        result.data = StorageAbstractImage.get(JSON.parse(result.data)?.storageabstractimage.id)
        return result
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage_abstract_image/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
