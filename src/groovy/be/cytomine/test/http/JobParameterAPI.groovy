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

import be.cytomine.processing.JobParameter
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage JobParameter to Cytomine with HTTP request during functional test
 */
class JobParameterAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        return doGET(URL, username, password)
    }

    static def listByJob(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/$id/parameter.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        def result = doPOST(URL,json,username,password)
        result.data = JobParameter.get(JSON.parse(result.data)?.jobparameter?.id)
        return result
    }

    static def update(def id, def jsonJobParameter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        return doPUT(URL,jsonJobParameter,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
