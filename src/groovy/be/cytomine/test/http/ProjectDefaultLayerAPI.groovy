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

import be.cytomine.project.ProjectDefaultLayer
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by hoyoux on 13.11.14.
 */
class ProjectDefaultLayerAPI extends DomainAPI {

    static def show(Long id, Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + idProject + "/defaultlayer/"+id+".json"
        return doGET(URL, username, password)
    }

    static def list(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + idProject + "/defaultlayer.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + JSON.parse(json)["project"] + "/defaultlayer.json"
        def result = doPOST(URL,json,username,password)
        result.data = ProjectDefaultLayer.get(JSON.parse(result.data)?.projectdefaultlayer?.id)
        return result
    }

    static def update(def id, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + JSON.parse(json)["project"] + "/defaultlayer/"+id+".json"
        return doPUT(URL,json,username,password)
    }

    static def delete(def id, Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + idProject + "/defaultlayer/"+id+".json"
        return doDELETE(URL,username,password)
    }
}
