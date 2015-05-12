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

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class UserPositionAPI extends DomainAPI {

    static def listLastByUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/position/${idUser}.json"
        return doGET(URL, username, password)
    }

    static def listLastByProject(Long idProject,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/project/$idProject/online.json"
        return doGET(URL, username, password)
    }

    static def listLastByImage(Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/online.json"
        return doGET(URL, username, password)
    }

    static def create(Long idImage, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/position.json"
        def result = doPOST(URL,json,username,password)
        return result
    }
}
